package com.exemplo.beerstock.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exemplo.beerstock.builder.BeerDTOBuilder;
import com.exemplo.beerstock.dto.BeerDTO;
import com.exemplo.beerstock.entity.Beer;
import com.exemplo.beerstock.exception.BeerAlreadyRegisteredException;
import com.exemplo.beerstock.mapper.BeerMapper;
import com.exemplo.beerstock.repository.BeerRepository;

@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

	private static final long INVALID_BEER_ID = 1L;
	
	@Mock
	private BeerRepository beerRepository;
	
	private BeerMapper beerMapper = BeerMapper.INSTANCE;
	
	@InjectMocks
	private BeerService beerService;
	
	@Test
	void whenBeerInformedThenItShulBeCreated() throws BeerAlreadyRegisteredException {
		//given
		BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedSavedBeer = beerMapper.toModel(beerDTO);
		
		//when
		when(beerRepository.findByName(beerDTO.getName())).thenReturn(Optional.empty());
		when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);
		
		//then
		BeerDTO createdBeerDTO = beerService.createBeer(beerDTO);
		
		assertEquals(beerDTO.getId(), createdBeerDTO.getId());
		assertEquals(beerDTO.getName(), createdBeerDTO.getName());
		
	}
	

}
