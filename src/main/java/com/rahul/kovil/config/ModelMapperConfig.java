package com.rahul.kovil.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true).setFieldMatchingEnabled(true).setAmbiguityIgnored(true)
				.setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

		// Skip all fields named id, tenantId, createdUser during mapping
		mapper.getConfiguration().setPropertyCondition(context -> {
//			String name = context.getMapping().getLastDestinationProperty().getName();
//			if ("tenantId".equals(name) || "createdUser".equals(name)) {
//				return false; // skip mapping this field
//			}
			return true; // map everything else
		});

		return mapper;
	}
}