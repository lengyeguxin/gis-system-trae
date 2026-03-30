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
        return addressRepository.save(address);
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
