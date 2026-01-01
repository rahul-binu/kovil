package com.rahul.kovil.user.service;


import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rahul.kovil.common.api.UserServiceApi;
import com.rahul.kovil.common.enums.BaseStatus;
import com.rahul.kovil.common.util.SiteHelper;
import com.rahul.kovil.common.dto.UserDto;
import com.rahul.kovil.user.entity.User;
import com.rahul.kovil.user.repository.UserRepository;

@Service
public class UserService implements UserServiceApi{
	
	@Autowired
	private ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private final SiteHelper siteHelper;
	
	private final UserRepository userRepository;
	
	public UserService(UserRepository userRepository, SiteHelper siteHelper) {
		this.userRepository = userRepository;
		this.siteHelper = siteHelper;
	}

    @Override
    public UserDto saveUser(UserDto userDto, String tenantId) {
        User user = modelMapper.map(userDto, User.class);
        user.setTenantId(tenantId);
        user.setTransId(siteHelper.transId(tenantId));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedEntity = userRepository.save(user);
        savedEntity.setPassword(userDto.getPassword());
        UserDto savedUserDto = modelMapper.map(savedEntity, UserDto.class);
        return savedUserDto;
    }

    @Override
    public UserDto getUserByUserName(String userName, BaseStatus status) {
        User user = userRepository.findByUserNameAndStatus(userName, status).orElse(new User());
        return modelMapper.map(user, UserDto.class);
    }
    
    @Override
    public void softDeleteLedger(String tenantId, String userId) {
    	User user = userRepository.findByTenantIdAndTransId(tenantId, userId);
    	if(user == null) throw new RuntimeException("Faild to find the user");
    	user.setStatus(BaseStatus.CANCELED);
    	userRepository.save(user);
    }
    
    
    
    
    
    
    
    
}
