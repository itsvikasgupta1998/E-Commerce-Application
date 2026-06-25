package com.app.repositories;


import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.app.entites.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
	Optional<User> findByEmailWithRoles(@Param("email") String email);

	boolean existsByEmail(String email);

	Optional<User> findByUserId(Long userId);

	boolean existsByEmailAndDeletedFalse(String email);

	Optional<User> findByUserIdAndDeletedFalse(Long userId);

	Page<User> findAllByDeletedFalse(Pageable pageable);

	Page<User> findAllByDeletedTrue(Pageable pageable);

	Optional<User> findByEmail(String email);


}