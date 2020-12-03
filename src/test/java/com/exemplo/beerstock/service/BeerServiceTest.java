package com.exemplo.beerstock.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
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
import com.exemplo.beerstock.exception.BeerNotFoundException;
import com.exemplo.beerstock.exception.BeerStockExceededException;
import com.exemplo.beerstock.mapper.BeerMapper;
import com.exemplo.beerstock.repository.BeerRepository;

import static org.hamcrest.Matchers.*;

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

		// given
		BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedSavedBeer = beerMapper.toModel(expectedBeerDTO);

		// when
		when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());
		when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);

		// then
		BeerDTO createdBeerDTO = beerService.createBeer(expectedBeerDTO);

		assertThat(createdBeerDTO.getId(), is(equalTo(expectedBeerDTO.getId())));
		assertThat(createdBeerDTO.getName(), is(equalTo(expectedBeerDTO.getName())));
		assertThat(createdBeerDTO.getQuantity(), is(equalTo(expectedBeerDTO.getQuantity())));
	}

	@Test
	void whenAlreadyRegisteredBeerInformedThenAnExceptionShouldBeThrow() {

		// given
		BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer duplicatedBeer = beerMapper.toModel(expectedBeerDTO);

		// when
		when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(duplicatedBeer));

		// then
		assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedBeerDTO));
	}

	@Test
	void whenValidBeerNameIsGivenThenReturnABeer() throws BeerNotFoundException {

		// given
		BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

		// when
		when(beerRepository.findByName(expectedFoundBeer.getName())).thenReturn(Optional.of(expectedFoundBeer));

		// then
		BeerDTO foundBeerDTO = beerService.findByName(expectedFoundBeerDTO.getName());

		assertThat(foundBeerDTO, is(equalTo(expectedFoundBeerDTO)));
	}

	@Test
	void whenNotRegisteredBeerNameIsGivenThenThrowAnException() {

		// given
		BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

		// when
		when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.empty());

		// then
		assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedFoundBeerDTO.getName()));
	}

	@Test
	void whenListBeerIsCalledThenReturnAListOfBeers() {

		// given
		BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

		// when
		when(beerRepository.findAll()).thenReturn(Collections.singletonList(expectedFoundBeer));

		// then
		List<BeerDTO> foundListBeersDTO = beerService.listAll();

		assertThat(foundListBeersDTO, is(not(empty())));
		assertThat(foundListBeersDTO.get(0), is(equalTo(expectedFoundBeerDTO)));
	}

	@Test
	void whenListBeerIsCalledThenReturnAnEmptyListOfBeers() {

		// when
		when(beerRepository.findAll()).thenReturn(Collections.emptyList());

		// then
		List<BeerDTO> foundListBeersDTO = beerService.listAll();

		assertThat(foundListBeersDTO, is(empty()));
	}

	@Test
	void whenExclusionIsCalledWithValidIdThenABeerShouldBeDeleted() throws BeerNotFoundException {
		
		// given
		BeerDTO expectedDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedDeletedBeer = beerMapper.toModel(expectedDeletedBeerDTO);

		// when
		when(beerRepository.findById(expectedDeletedBeerDTO.getId())).thenReturn(Optional.of(expectedDeletedBeer));
		doNothing().when(beerRepository).deleteById(expectedDeletedBeerDTO.getId());

		// then
		beerService.deleteById(expectedDeletedBeerDTO.getId());

		verify(beerRepository, times(1)).findById(expectedDeletedBeerDTO.getId());
		verify(beerRepository, times(1)).deleteById(expectedDeletedBeerDTO.getId());
	}

	@Test
	void whenIncrementIsCalledThenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
		
		// given
		BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

		// when
		when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
		when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

		int quantityToIncrement = 10;
		int expectedQuantityAfterIncrement = expectedBeerDTO.getQuantity() + quantityToIncrement;

		// then
		BeerDTO incrementedBeerDTO = beerService.increment(expectedBeerDTO.getId(), quantityToIncrement);

		assertThat(expectedQuantityAfterIncrement, equalTo(incrementedBeerDTO.getQuantity()));
		assertThat(expectedQuantityAfterIncrement, lessThan(expectedBeerDTO.getMax()));
	}

}
