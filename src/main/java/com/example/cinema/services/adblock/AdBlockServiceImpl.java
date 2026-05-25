package com.example.cinema.services.adblock;

import com.example.cinema.dtos.adblock.AdBlockNowResponse;
import com.example.cinema.dtos.adblock.AdBlockRequest;
import com.example.cinema.dtos.adblock.AdBlockResponse;
import com.example.cinema.events.adblock.AdBlockCreatedEvent;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.kafka.CinemaEventProducer;
import com.example.cinema.models.cinema.*;
import com.example.cinema.models.enums.WalletTxType;
import com.example.cinema.repositories.cinema.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AdBlockServiceImpl implements AdBlockService{
    private final AdBlockRepository adBlockRepository;
    private final AdBlockPricingRepository adBlockPricingRepository;
    private final CinemaRepository cinemaRepository;
    private final CinemaWalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final CinemaEventProducer eventProducer;

    public AdBlockServiceImpl(AdBlockRepository adBlockRepository, AdBlockPricingRepository adBlockPricingRepository, CinemaRepository cinemaRepository, CinemaWalletRepository walletRepository, WalletTransactionRepository walletTransactionRepository, CinemaEventProducer eventProducer) {
        this.adBlockRepository = adBlockRepository;
        this.adBlockPricingRepository = adBlockPricingRepository;
        this.cinemaRepository = cinemaRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.eventProducer = eventProducer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdBlockResponse createAdBlock(UUID cinemaId, AdBlockRequest request) throws ResourceNotFoundException, ConflictException {
        // Validar que el cine exista
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cine no encontrado"));

        // Obtenemos el ultimo bloque activo para calcular el nuevo bloque
        List<AdBlock> activeBlocks = adBlockRepository.findByCinema_IdOrderByStartDateDesc(cinemaId);
        AdBlock lastBlock = activeBlocks.isEmpty() ? null : activeBlocks.get(0);

        // Calculamos las fechas del nuevo bloque
        LocalDate startDate = lastBlock == null ? LocalDate.now() : lastBlock.getEndDate().plusDays(1);
        LocalDate endDate = startDate.plusDays(request.getDaysBlocked() - 1);
       //Obtenemos el precio del bloqueo
        AdBlockPricing pricing = adBlockPricingRepository.findByCinemaId(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Precio para el bloqueo no encontrado"));

        BigDecimal amountToPay = pricing.getPricePerDay().multiply(BigDecimal.valueOf(request.getDaysBlocked()));

        // Validar que el cine tenga fondos suficientes
        if (walletRepository.getBalanceByCinemaId(cinemaId).compareTo(amountToPay) < 0) {
            throw new ConflictException("Fondos insuficientes para bloquear anuncios");
        }

        // Registrar la transaccion en el wallet
        CinemaWallet wallet = walletRepository.findByCinema_Id(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cartera del cine no encontrado"));


        // Crear el bloqueo de anuncios
        AdBlock adBlock = new AdBlock();
        adBlock.setCinema(cinema);
        adBlock.setDaysBlocked(request.getDaysBlocked());
        adBlock.setStartDate(startDate);
        adBlock.setEndDate(endDate);
        adBlock.setAmountPaid(amountToPay);
        AdBlock adBlockSaved =adBlockRepository.save(adBlock);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setCinemaWallet(wallet);
        transaction.setAmount(amountToPay);
        transaction.setType(WalletTxType.PAYMENT);
        transaction.setDescription("Pago por bloqueo de anuncios: " + request.getDaysBlocked() + " días");
        walletTransactionRepository.save(transaction);

        // Actualizar el balance del wallet
        wallet.setBalance(wallet.getBalance().subtract(amountToPay));
        walletRepository.save(wallet);

        // Publicar evento de bloqueo de anuncios creado
        AdBlockCreatedEvent event = AdBlockCreatedEvent.fromEntity(adBlockSaved);
        eventProducer.publishAdBlockCreated(event);

        return AdBlockResponse.fromEntity(adBlockSaved);
    }

    @Override
    public List<AdBlockResponse> getAdBlocksByCinemaId(UUID cinemaId) throws ResourceNotFoundException {
        List<AdBlock> adBlocks = adBlockRepository.findByCinema_IdOrderByStartDateDesc(cinemaId);

        return adBlocks.stream()
                .map(AdBlockResponse::fromEntity)
                .toList();


    }

    @Override
    public List<AdBlockResponse> getAllAdBlocks() {
        List<AdBlock> adBlocks = adBlockRepository.findAll();

        return adBlocks.stream()
                .map(AdBlockResponse::fromEntity)
                .toList();
    }

    @Override
    public AdBlockNowResponse getCurrentAdBlockStatus(UUID cinemaId) throws ResourceNotFoundException {
        List<AdBlock> activeBlocks = adBlockRepository.findActiveByCinemaIdAndDate(cinemaId, LocalDate.now());

        boolean isBlocked = !activeBlocks.isEmpty();
        LocalDate blockEndDate = isBlocked ? activeBlocks.get(0).getEndDate() : null;

        return AdBlockNowResponse.blocked(isBlocked, blockEndDate);
    }
}
