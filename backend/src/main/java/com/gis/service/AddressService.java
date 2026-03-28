package com.gis.service;

import com.gis.entity.Address;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface AddressService {

    // 获取所有地址
    List<Address> getAllAddresses();

    // 根据ID获取地址
    Optional<Address> getAddressById(@NonNull Long id);

    // 保存地址
    Address saveAddress(@NonNull Address address);

    // 删除地址
    void deleteAddress(@NonNull Long id);

    // 按行政区划查询地址
    List<Address> getAddressesByDistrict(@NonNull String district);

    // 搜索地址
    List<Address> searchAddresses(@NonNull String keyword);
}
