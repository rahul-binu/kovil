package com.rahul.kovil.inventory.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.inventory.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {

	List<Item> findByTenantIdAndStatus(String tenantId, BaseStatus status);

	Optional<Item> findByIdAndStatus(Long id, BaseStatus status);

	Item findByItemCodeAndTenantIdInAndStatus(String code, List<String> tenatIds, BaseStatus status);

	boolean existsByItemCodeAndTenantIdInAndStatus(String itemCode, List<String> tenantScope, BaseStatus active);

	@Query("SELECT i.id, i.itemCode, i.itemName, i.itemGroup, i.baseUnit FROM Item i WHERE i.id IN :itemIds")
	List<Object[]> findItemForStockByItemIdIn(Set<Long> itemIds);

}
