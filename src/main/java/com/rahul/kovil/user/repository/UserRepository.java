package com.rahul.kovil.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUserNameAndStatus(String userName, BaseStatus status);

	User findByTenantIdAndTransId(String tenantId, String userId);

}
