package edu.ntnu.idatt2106.boco.service;

import edu.ntnu.idatt2106.boco.models.Item;
import edu.ntnu.idatt2106.boco.models.Rental;
import edu.ntnu.idatt2106.boco.models.User;
import edu.ntnu.idatt2106.boco.payload.request.RegisterRentalRequest;
import edu.ntnu.idatt2106.boco.payload.response.RentalResponse;
import edu.ntnu.idatt2106.boco.repository.ItemRepository;
import edu.ntnu.idatt2106.boco.repository.RentalRepository;
import edu.ntnu.idatt2106.boco.repository.UserRepository;
import edu.ntnu.idatt2106.boco.util.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * A class that represents a RentalService
 */

@Service
public class RentalService
{
    @Autowired
    RentalRepository rentalRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

    /**
     * A method for creating a rental request
     * status is set later, when owner of item is responding to the request
     * @param request the rental request that is being stored to database
     * @return returns a status int
     * */
    public RentalResponse registerRental(RegisterRentalRequest request)
    {
        Optional<User> optionalUser = userRepository.findById(request.getUserId());
        if (optionalUser.isEmpty()) return null;
        User user = optionalUser.get();

        Optional<Item> optionalItem = itemRepository.findById(request.getItemId());
        if (optionalItem.isEmpty()) return null;
        Item item = optionalItem.get();

        Rental rental = new Rental(
                request.getMessage(),
                request.getStartDate(),
                request.getEndDate(),
                "PENDING",
                user,
                item,
                request.getDeliveryInfo()
        );
        rental = rentalRepository.save(rental);
        return Mapper.ToRentalResponse(rental);
    }

    /**
     * A method for retrieving all rental requests for a specific item
     * @param itemId the itemId the rentalRequests belongs to
     * @return returns a list of rentalRequests of an item
     */
    public List<RentalResponse> getAllRentalsForItem(long itemId)
    {
        Optional<Item> optionalItem = itemRepository.findById(itemId);
        if (optionalItem.isEmpty()) return null;
        Item item = optionalItem.get();

        List<Rental> rentals = rentalRepository.findAllByItem(item);
        return Mapper.ToRentalResponses(rentals);
    }

    /**
     * A method for accepting a rental request based on rentalId
     * @param rentalId the rentalId that is being updated
     * @return returns the renewed Rental object
     */
    public RentalResponse acceptRental(long rentalId)
    {
        return updateRentalStatus(rentalId, "ACCEPTED");
    }

    /**
     * A method for rejecting a rental request based on rentalId
     * @param rentalId the rentalId that is being updated
     * @return returns the renewed Rental object
     */
    public RentalResponse rejectRental(long rentalId)
    {
        return updateRentalStatus(rentalId, "REJECTED");
    }

    /**
     * A method for canceling a rental request based on rentalId
     * @param rentalId the rentalId that is being updated
     * @return returns the renewed Rental object
     */
    public RentalResponse cancelRental(long rentalId)
    {
        return updateRentalStatus(rentalId, "CANCELED");
    }

    /**
     * A method for updating a rental request based on rentalId
     * @param rentalId the rentalId that is being updated
     * @param status the new status
     * @return returns the renewed Rental object 
     */
    private RentalResponse updateRentalStatus(long rentalId, String status)
    {
        Optional<Rental> optionalRental = rentalRepository.findById(rentalId);
        if (optionalRental.isEmpty()) return null;
        Rental rental = optionalRental.get();

        rental.setStatus(status);
        rental = rentalRepository.save(rental);
        return Mapper.ToRentalResponse(rental);
    }

    public RentalResponse getRental(long rentalId)
    {
        Optional<Rental> optionalRental = rentalRepository.findById(rentalId);
        if (optionalRental.isEmpty()) return null;
        return Mapper.ToRentalResponse(optionalRental.get());
    }
}
