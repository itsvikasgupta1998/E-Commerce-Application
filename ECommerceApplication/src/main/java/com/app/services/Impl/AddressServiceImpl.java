package com.app.services.Impl;

import java.util.List;
import com.app.exceptions.APIException;
import com.app.mappers.AddressMapper;
import com.app.payloads.AddressRequest;
import com.app.payloads.AddressResponse;
import com.app.services.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.app.entites.Address;
import com.app.exceptions.ResourceNotFoundException;
import com.app.repositories.AddressRepository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

	private final AddressRepository addressRepository;
	private final AddressMapper addressMapper;

	@Override
	public AddressResponse createAddress(AddressRequest request) {

		log.info(
				"Creating address for city={}, state={}, pincode={}",
				request.getCity(), request.getState(), request.getPincode());

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

					log.warn("Duplicate address creation attempt detected for city={}, pincode={}",
							request.getCity(),
							request.getPincode());
					throw new APIException("Address already exists");
				});

		Address address = addressMapper.toEntity(request);
		Address saved = addressRepository.save(address);
		log.info("Address created successfully. addressId={}", saved.getAddressId());
		return addressMapper.toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public AddressResponse getAddressById(Long addressId) {
		log.debug("Fetching address with id={}", addressId);
		Address address = addressRepository.findById(addressId)
						.orElseThrow(() -> {
							log.warn("Address not found. addressId={}", addressId);
							return new ResourceNotFoundException("Address", "addressId", addressId);
						});
		log.debug("Address fetched successfully. addressId={}", addressId);
		return addressMapper.toResponse(address);
	}

	@Override
	@Transactional(readOnly = true)
	public List<AddressResponse> getAllAddresses() {
		log.debug("Fetching all addresses");
		List<AddressResponse> addresses = addressRepository
				.findAll()
				.stream()
				.map(addressMapper::toResponse)
				.toList();
		log.debug("Total addresses fetched={}", addresses.size());
		return addresses;
	}

	@Override
	public AddressResponse updateAddress(Long addressId, AddressRequest request) {
		log.info("Updating address. addressId={}", addressId);
		Address address = addressRepository.findById(addressId)
						.orElseThrow(() -> {
							log.warn("Address not found for update. addressId={}", addressId);
							return new ResourceNotFoundException("Address", "addressId", addressId);
						});
		addressMapper.updateEntity(request, address);
		Address updated = addressRepository.save(address);
		log.info("Address updated successfully. addressId={}", updated.getAddressId());
		return addressMapper.toResponse(updated);
	}

	@Override
	public void deleteAddress(Long addressId) {
		log.info("Deleting address. addressId={}", addressId);
		Address address = addressRepository.findById(addressId)
						.orElseThrow(() -> {
							log.warn("Address not found for deletion. addressId={}", addressId);
							return new ResourceNotFoundException("Address", "addressId", addressId);
						});
		addressRepository.delete(address);
		log.info("Address deleted successfully. addressId={}", addressId);
	}


}
