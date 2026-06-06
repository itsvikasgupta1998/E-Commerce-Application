package com.app.services;

import java.util.List;

import com.app.mappers.AddressMapper;
import com.app.payloads.AddressRequest;
import com.app.payloads.AddressResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.app.entites.Address;
import com.app.exceptions.ResourceNotFoundException;
import com.app.repositories.AddressRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

	private final AddressRepository addressRepository;
	private final AddressMapper addressMapper;

	@Override
	public AddressResponse createAddress(
			AddressRequest request
	) {

		addressRepository
				.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(
						request.getCountry(),
						request.getState(),
						request.getCity(),
						request.getPincode(),
						request.getStreet(),
						request.getBuildingName()
				)
				.ifPresent(address -> {
					throw new IllegalStateException(
							"Address already exists"
					);
				});

		Address address =
				addressMapper.toEntity(request);

		Address saved =
				addressRepository.save(address);

		return addressMapper.toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public AddressResponse getAddressById(
			Long addressId
	) {

		Address address =
				addressRepository.findById(addressId)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"Address",
										"addressId",
										addressId
								));

		return addressMapper.toResponse(address);
	}

	@Override
	@Transactional(readOnly = true)
	public List<AddressResponse> getAllAddresses() {

		return addressRepository.findAll()
				.stream()
				.map(addressMapper::toResponse)
				.toList();
	}

	@Override
	public AddressResponse updateAddress(
			Long addressId,
			AddressRequest request
	) {

		Address address =
				addressRepository.findById(addressId)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"Address",
										"addressId",
										addressId
								));

		addressMapper.updateEntity(
				request,
				address
		);

		Address updated =
				addressRepository.save(address);

		return addressMapper.toResponse(updated);
	}

	@Override
	public void deleteAddress(
			Long addressId
	) {

		Address address =
				addressRepository.findById(addressId)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"Address",
										"addressId",
										addressId
								));

		addressRepository.delete(address);
	}
}