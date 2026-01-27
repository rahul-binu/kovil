package com.rahul.kovil.vendor.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.enums.VendorType;
import com.rahul.kovil.vendor.entity.Vendor;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long>{

	List<Vendor> findByTypeAndFullNameContainingIgnoreCaseOrTypeAndMobileContainingIgnoreCaseOrTypeAndFamilyNameContainingIgnoreCase(
	        VendorType type1, String name, VendorType type2, String mobile, VendorType type3, String familyName);

	@Query("""
		    SELECT 
		        FUNCTION('MONTH', pt.createdAt),
		        COUNT(pt.id)
		    FROM Vendor pt
		    WHERE pt.createdAt BETWEEN :fromDate AND :toDate
		      AND pt.status = :status
		      AND pt.tenantId = :tenantId
		    GROUP BY FUNCTION('MONTH', pt.createdAt)
		    ORDER BY FUNCTION('MONTH', pt.createdAt)
		""")
	List<Object[]> findNoOfDevoteeRegistarByMonth(LocalDateTime fromDate, LocalDateTime toDate, BaseStatus status,
			String tenantId);

	
	Vendor findByTransId(String transId);

	List<Vendor> findByTransIdIn(List<String> vids);
}
