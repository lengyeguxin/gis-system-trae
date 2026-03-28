package com.gis.controller;

import com.gis.entity.Address;
import com.gis.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:4173", "http://localhost:3000", "http://localhost:3002", "http://localhost:5176"})
public class AddressController {

    @Autowired
    private AddressService addressService;

    // 获取所有地址
    @GetMapping
    public ResponseEntity<List<Address>> getAllAddresses() {
        List<Address> addresses = addressService.getAllAddresses();
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    // 根据ID获取地址
    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddressById(@PathVariable @NonNull Long id) {
        return addressService.getAddressById(id)
                .map(address -> new ResponseEntity<>(address, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 新增地址
    @PostMapping
    public ResponseEntity<Address> createAddress(@RequestBody @NonNull Address address) {
        Address savedAddress = addressService.saveAddress(address);
        return new ResponseEntity<>(savedAddress, HttpStatus.CREATED);
    }

    // 更新地址
    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(@PathVariable @NonNull Long id, @RequestBody @NonNull Address address) {
        return addressService.getAddressById(id)
                .map(existingAddress -> {
                    address.setId(id);
                    Address updatedAddress = addressService.saveAddress(address);
                    return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 删除地址
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable @NonNull Long id) {
        if (addressService.getAddressById(id).isPresent()) {
            addressService.deleteAddress(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 按行政区划查询地址
    @GetMapping("/district/{district}")
    public ResponseEntity<List<Address>> getAddressesByDistrict(@PathVariable @NonNull String district) {
        List<Address> addresses = addressService.getAddressesByDistrict(district);
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    // 搜索地址
    @GetMapping("/search")
    public ResponseEntity<List<Address>> searchAddresses(@RequestParam @NonNull String keyword) {
        List<Address> addresses = addressService.searchAddresses(keyword);
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }
}
