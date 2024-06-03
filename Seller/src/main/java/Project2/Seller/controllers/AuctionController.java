package Project2.Seller.controllers;

import Project2.Seller.models.AuctionRequest;
import Project2.Seller.models.AuctionRequestResponse;
import Project2.Seller.services.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("itemforsale")
public class AuctionController {
    @Autowired
    private AuctionService auctionService;

    @PostMapping()
    public AuctionRequestResponse processAuctionRequest(@RequestBody AuctionRequest auctionRequest) {
        return auctionService.processAuctionRequest(auctionRequest);
    }
}
