<template>
  <div class="video-player-overlay" v-if="visible">
    <div class="video-player-container">
      <div class="video-player-header">
        <h3>{{ title }}</h3>
        <button class="close-btn" @click="close">关闭</button>
      </div>
      <div class="video-player-body">
        <div class="video-wrapper" ref="videoWrapper">
          <video ref="videoElement" muted autoplay playsinline></video>
          <div class="video-loading" v-if="loading">
            <div class="loading-spinner"></div>
            <p>正在连接视频流...</p>
          </div>
          <div class="video-error" v-if="error">
            <p>{{ error }}</p>
            <button @click="retry">重试</button>
          </div>
        </div>
        <div class="video-controls">
          <button class="control-btn" @click="takeSnapshot" :disabled="!isPlaying">
            截图
          </button>
          <button class="control-btn" @click="toggleFullscreen">
            {{ isFullscreen ? '退出全屏' : '全屏' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import Hls from 'hls.js'
import axios from 'axios'

export default {
  name: 'VideoPlayer',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    cameraId: {
      type: Number,
      default: null
    },
    title: {
      type: String,
      default: '实时视频'
    }
  },
  emits: ['close'],
  setup(props, { emit }) {
    const videoElement = ref(null)
    const videoWrapper = ref(null)
    const loading = ref(true)
    const error = ref('')
    const isPlaying = ref(false)
    const isFullscreen = ref(false)
    
    let hls = null
    let streamCheckInterval = null

    const startStream = async () => {
      if (!props.cameraId) return
      
      loading.value = true
      error.value = ''
      
      try {
        const response = await axios.post(`/api/video/stream/${props.cameraId}`)
        if (response.data.success) {
          const streamUrl = response.data.streamUrl
          await playHlsStream(streamUrl)
        } else {
          error.value = response.data.message || '启动视频流失败'
          loading.value = false
        }
      } catch (err) {
        error.value = '连接视频流失败: ' + (err.response?.data?.message || err.message)
        loading.value = false
      }
    }

    const playHlsStream = async (streamUrl) => {
      await nextTick()
      
      if (!videoElement.value) {
        error.value = '视频元素未初始化'
        loading.value = false
        return
      }

      const video = videoElement.value
      const fullUrl = 'http://localhost:8081' + streamUrl

      if (Hls.isSupported()) {
        hls = new Hls({
          liveDurationInfinity: true,
          liveBackBufferLength: 0,
          maxBufferLength: 10,
          maxMaxBufferLength: 30,
          enableWorker: true,
          lowLatencyMode: true
        })
        
        hls.loadSource(fullUrl)
        hls.attachMedia(video)
        
        hls.on(Hls.Events.MANIFEST_PARSED, () => {
          loading.value = false
          video.play().catch(e => console.log('Auto play failed:', e))
        })
        
        hls.on(Hls.Events.ERROR, (event, data) => {
          if (data.fatal) {
            switch (data.type) {
              case Hls.ErrorTypes.NETWORK_ERROR:
                error.value = '网络连接错误，请检查视频源'
                break
              case Hls.ErrorTypes.MEDIA_ERROR:
                hls.recoverMediaError()
                break
              default:
                error.value = '视频播放错误'
                hls.destroy()
                break
            }
            loading.value = false
          }
        })
      } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
        video.src = fullUrl
        video.addEventListener('loadedmetadata', () => {
          loading.value = false
          video.play().catch(e => console.log('Auto play failed:', e))
        })
      } else {
        error.value = '浏览器不支持 HLS 播放'
        loading.value = false
      }

      video.addEventListener('playing', () => {
        isPlaying.value = true
      })
      
      video.addEventListener('pause', () => {
        isPlaying.value = false
      })
    }

    const stopStream = async () => {
      if (hls) {
        hls.destroy()
        hls = null
      }
      
      if (streamCheckInterval) {
        clearInterval(streamCheckInterval)
        streamCheckInterval = null
      }
      
      if (props.cameraId) {
        try {
          await axios.delete(`/api/video/stream/${props.cameraId}`)
        } catch (err) {
          console.log('Stop stream error:', err)
        }
      }
      
      loading.value = true
      error.value = ''
      isPlaying.value = false
    }

    const close = () => {
      stopStream()
      emit('close')
    }

    const retry = () => {
      error.value = ''
      startStream()
    }

    const takeSnapshot = () => {
      if (!videoElement.value || !isPlaying.value) return
      
      const video = videoElement.value
      const canvas = document.createElement('canvas')
      canvas.width = video.videoWidth
      canvas.height = video.videoHeight
      
      const ctx = canvas.getContext('2d')
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
      
      const link = document.createElement('a')
      link.download = `snapshot_${props.cameraId}_${Date.now()}.png`
      link.href = canvas.toDataURL('image/png')
      link.click()
    }

    const toggleFullscreen = async () => {
      if (!videoWrapper.value) return
      
      try {
        if (!document.fullscreenElement) {
          await videoWrapper.value.requestFullscreen()
          isFullscreen.value = true
        } else {
          await document.exitFullscreen()
          isFullscreen.value = false
        }
      } catch (err) {
        console.log('Fullscreen error:', err)
      }
    }

    watch(() => props.visible, async (newVal) => {
      if (newVal && props.cameraId) {
        await nextTick()
        startStream()
      } else if (!newVal) {
        stopStream()
      }
    })

    watch(() => props.cameraId, async (newId, oldId) => {
      if (oldId && props.visible) {
        await stopStream()
      }
      if (newId && props.visible) {
        await nextTick()
        startStream()
      }
    })

    onUnmounted(() => {
      stopStream()
    })

    return {
      videoElement,
      videoWrapper,
      loading,
      error,
      isPlaying,
      isFullscreen,
      close,
      retry,
      takeSnapshot,
      toggleFullscreen
    }
  }
}
</script>

<style scoped>
.video-player-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.video-player-container {
  background: #fff;
  border-radius: 8px;
  width: 640px;
  max-width: 90vw;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.video-player-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
}

.video-player-header h3 {
  margin: 0;
  color: #165DFF;
  font-size: 16px;
}

.close-btn {
  background: #F53F3F;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.close-btn:hover {
  background: #E53838;
}

.video-player-body {
  padding: 16px;
}

.video-wrapper {
  background: #1a1a1a;
  border-radius: 4px;
  position: relative;
  width: 100%;
  height: 360px;
}

.video-wrapper video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.video-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #fff;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #333;
  border-top-color: #165DFF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.video-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #fff;
}

.video-error button {
  margin-top: 12px;
  background: #165DFF;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.video-controls {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.control-btn {
  background: #f0f0f0;
  border: 1px solid #d9d9d9;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.control-btn:hover:not(:disabled) {
  background: #e6e6e6;
}

.control-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.video-wrapper:fullscreen {
  width: 100vw;
  height: 100vh;
}

.video-wrapper:fullscreen video {
  width: 100%;
  height: 100%;
}
</style>