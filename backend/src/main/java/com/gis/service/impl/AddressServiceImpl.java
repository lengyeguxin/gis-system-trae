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
                .filter(address -> district.equals(address.getAdmin_code()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Address> searchAddresses(@NonNull String keyword) {
        return addressRepository.findAll().stream()
                .filter(address -> address.getAddress_full().contains(keyword) ||
                        address.getAdmin_code().contains(keyword) ||
                        address.getStreet().contains(keyword) ||
                        address.getHouse_number().contains(keyword))
                .collect(Collectors.toList());
    }
}
