package me.optif1ne.auction.storage;

import me.optif1ne.auction.model.AuctionItem;

import java.io.IOException;
import java.util.List;

public interface Storage {
    List<AuctionItem> load() throws IOException;
    void save(List<AuctionItem> lots) throws IOException;
}
