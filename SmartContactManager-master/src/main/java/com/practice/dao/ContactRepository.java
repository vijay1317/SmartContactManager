package com.practice.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.practice.entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer>{

	@Query("from Contact as c where c.user.id= :userId")
	// pageable required 2 thinks 1no of contact in page and current page number, IN return it will send the page required 
	public Page<Contact> findContactsByUser(@Param("userId")int userId,Pageable pageable );
}
