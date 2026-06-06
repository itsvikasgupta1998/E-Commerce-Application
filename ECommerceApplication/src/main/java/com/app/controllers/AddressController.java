package com.app.controllers;

import java.util.List;
import com.app.payloads.AddressRequest;
import com.app.payloads.AddressResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.app.services.AddressService;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Address APIs")
public class AddressController {

	private final AddressService addressService;

	@PostMapping
	public ResponseEntity<AddressResponse> createAddress(
			@Valid @RequestBody AddressRequest request
	) {

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(
						addressService.createAddress(
								request
						)
				);
	}

	@GetMapping
	public ResponseEntity<List<AddressResponse>>
	getAllAddresses() {

		return ResponseEntity.ok(
				addressService.getAllAddresses()
		);
	}

	@GetMapping("/{addressId}")
	public ResponseEntity<AddressResponse>
	getAddress(
			@PathVariable Long addressId
	) {

		return ResponseEntity.ok(
				addressService.getAddressById(
						addressId
				)
		);
	}

	@PutMapping("/{addressId}")
	public ResponseEntity<AddressResponse>
	updateAddress(
			@PathVariable Long addressId,
			@Valid @RequestBody AddressRequest request
	) {

		return ResponseEntity.ok(
				addressService.updateAddress(
						addressId,
						request
				)
		);
	}

	@DeleteMapping("/{addressId}")
	public ResponseEntity<Void>
	deleteAddress(
			@PathVariable Long addressId
	) {

		addressService.deleteAddress(
				addressId
		);

		return ResponseEntity
				.noContent()
				.build();
	}
}
