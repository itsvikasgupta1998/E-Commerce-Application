package com.app.services;

import com.app.payloads.AddressRequest;
import com.app.payloads.AddressResponse;
import java.util.List;


public interface AddressService {

	AddressResponse createAddress(
			AddressRequest request
	);

	AddressResponse getAddressById(
			Long addressId
	);

	List<AddressResponse> getAllAddresses();

	AddressResponse updateAddress(
			Long addressId,
			AddressRequest request
	);

	void deleteAddress(
			Long addressId
	);
}