package com.gis.service;

import com.gis.entity.Address;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface AddressService {

    List<Address> getAllAddresses();

    Optional<Address> getAddressById(@NonNull Long id);

    Address saveAddress(@NonNull Address address);

    void deleteAddress(@NonNull Long id);

    List<Address> getAddressesByDistrict(@NonNull String district);

    List<Address> searchAddresses(@NonNull String keyword);

    List<Address> searchAddresses(@NonNull String keyword, @NonNull String mode);
}
