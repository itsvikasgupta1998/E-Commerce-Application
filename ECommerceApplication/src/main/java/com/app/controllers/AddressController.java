package com.app.controllers;

import java.util.List;

import com.app.payloads.AddressRequest;
import com.app.payloads.AddressResponse;
import com.app.services.AddressService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Address APIs")
public class AddressController {

	private final AddressService addressService;

	// ---------------- CREATE ADDRESS ----------------
	@PostMapping
	public ResponseEntity<AddressResponse> createAddress(
			@Valid @RequestBody AddressRequest request
	) {

		AddressResponse response = addressService.createAddress(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// ---------------- GET ALL ADDRESSES ----------------
	@GetMapping
	public ResponseEntity<List<AddressResponse>> getAllAddresses() {

		List<AddressResponse> response = addressService.getAllAddresses();

		return ResponseEntity.ok(response);
	}

	// ---------------- GET ADDRESS BY ID ----------------
	@GetMapping("/{addressId}")
	public ResponseEntity<AddressResponse> getAddress(@PathVariable Long addressId) {

		AddressResponse response = addressService.getAddressById(addressId);

		return ResponseEntity.ok(response);
	}

	// ---------------- UPDATE ADDRESS ----------------
	@PutMapping("/{addressId}")
	public ResponseEntity<AddressResponse> updateAddress(
			@PathVariable Long addressId,
			@Valid @RequestBody AddressRequest request
	) {

		AddressResponse response =
				addressService.updateAddress(addressId, request);

		return ResponseEntity.ok(response);
	}

	// ---------------- DELETE ADDRESS ----------------
	@DeleteMapping("/{addressId}")
	public ResponseEntity<Void> deleteAddress(@PathVariable Long addressId) {

		addressService.deleteAddress(addressId);

		return ResponseEntity.noContent().build();
	}
}