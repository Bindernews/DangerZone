package com.vortexel.dangerzone.api.trading;

import javax.annotation.Nonnull;

public interface IMerchandiseRegistry {

    /**
     * Register a new offer.
     */
    void register(@Nonnull Offer offer);

    /**
     * Get the offer at the given index.
     * @param index
     * @return
     */
    Offer getOffer(int index);

    /**
     * Get the total number of offers.
     * @return
     */
    int getTotalOffers();

    /**
     * Returns true if {@code offer} is already registered.
     * @param offer the offer to check
     */
    boolean contains(@Nonnull Offer offer);
}
