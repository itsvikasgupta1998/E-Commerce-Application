package com.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.app.entites.Address;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

	Optional<Address> findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(
			String country,
			String state,
			String city,
			String pincode,
			String street,
			String buildingName
	);
}