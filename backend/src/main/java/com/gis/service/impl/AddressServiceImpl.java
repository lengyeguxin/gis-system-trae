package com.gis.service.impl;

import com.gis.entity.Address;
import com.gis.repository.AddressRepository;
import com.gis.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public List<Address> getAllAddresses() {
        return addressRepository.findAll();
    }

    @Override
    public Optional<Address> getAddressById(@NonNull Long id) {
        return addressRepository.findById(id);
    }

    @Override
    public Address saveAddress(@NonNull Address address) {
        List<Address> existingAddresses = addressRepository.findAll();
        
        for (Address existing : existingAddresses) {
            if (address.getId() != null && address.getId().equals(existing.getId())) {
                continue;
            }
            
            if (isDuplicateAddress(address, existing)) {
                throw new RuntimeException("地址重复：该地址与现有地址「" + existing.getAddress_full() + "」重复");
            }
            
            if (isSameLocation(address, existing)) {
                throw new RuntimeException("经纬度重复：该地址的经纬度与现有地址「" + existing.getAddress_full() + "」相同");
            }
        }
        
        return addressRepository.save(address);
    }
    
    private boolean isDuplicateAddress(Address newAddress, Address existingAddress) {
        String newName = normalizeAddressName(newAddress.getAddress_full());
        String existingName = normalizeAddressName(existingAddress.getAddress_full());
        return newName.equals(existingName);
    }
    
    private String normalizeAddressName(String addressName) {
        if (addressName == null) return "";
        String normalized = addressName.trim();
        normalized = normalized.replaceAll("^(北京市|天津市|上海市|重庆市|河北省|山西省|辽宁省|吉林省|黑龙江省|江苏省|浙江省|安徽省|福建省|江西省|山东省|河南省|湖北省|湖南省|广东省|海南省|四川省|贵州省|云南省|陕西省|甘肃省|青海省|台湾省|内蒙古自治区|广西壮族自治区|西藏自治区|宁夏回族自治区|新疆维吾尔自治区|香港特别行政区|澳门特别行政区)", "");
        normalized = normalized.replaceAll(".*?市", "");
        normalized = normalized.replaceAll(".*?区", "");
        normalized = normalized.replaceAll(".*?县", "");
        normalized = normalized.replaceAll("\\s+", "");
        return normalized;
    }
    
    private boolean isSameLocation(Address newAddress, Address existingAddress) {
        if (newAddress.getLat() == 0 || newAddress.getLon() == 0) return false;
        if (existingAddress.getLat() == 0 || existingAddress.getLon() == 0) return false;
        
        double latDiff = Math.abs(newAddress.getLat() - existingAddress.getLat());
        double lonDiff = Math.abs(newAddress.getLon() - existingAddress.getLon());
        
        return latDiff < 0.000001 && lonDiff < 0.000001;
    }

    @Override
    public void deleteAddress(@NonNull Long id) {
        addressRepository.deleteById(id);
    }

    @Override
    public List<Address> getAddressesByDistrict(@NonNull String district) {
        return addressRepository.findAll().stream()
                .filter(address -> address.getAdmin_code() != null && district.equals(address.getAdmin_code()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Address> searchAddresses(@NonNull String keyword) {
        return searchAddresses(keyword, "fuzzy");
    }

    @Override
    public List<Address> searchAddresses(@NonNull String keyword, @NonNull String mode) {
        if ("exact".equals(mode)) {
            return addressRepository.findAll().stream()
                    .filter(address -> (address.getAddress_full() != null && address.getAddress_full().equals(keyword)) ||
                            (address.getAdmin_code() != null && address.getAdmin_code().equals(keyword)) ||
                            (address.getStreet() != null && address.getStreet().equals(keyword)) ||
                            (address.getAdmin_name() != null && address.getAdmin_name().equals(keyword)))
                    .collect(Collectors.toList());
        } else {
            return addressRepository.findAll().stream()
                    .filter(address -> (address.getAddress_full() != null && address.getAddress_full().contains(keyword)) ||
                            (address.getAdmin_code() != null && address.getAdmin_code().contains(keyword)) ||
                            (address.getStreet() != null && address.getStreet().contains(keyword)) ||
                            (address.getAdmin_name() != null && address.getAdmin_name().contains(keyword)))
                    .collect(Collectors.toList());
        }
    }
}
