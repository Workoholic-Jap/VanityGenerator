package com.fatsoapps.vanitygenerator.core.query;

import com.fatsoapps.vanitygenerator.core.network.IllegalDecimalVersionException;
import com.fatsoapps.vanitygenerator.core.network.Network;
import com.fatsoapps.vanitygenerator.core.network.GlobalNetParams;
import org.bitcoinj.core.ECKey;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * QueryPool is collection of Query's defined by the user that can be accessed anywhere by calling getInstance().
 * General usage is to create threads that rely on the Query's within QueryPool and to add / update / delete
 * accordingly. RegexQuery's are not included in this class since each RQ can contain multiple sub-queries which cannot
 * be easily handled and removed when a match is found.
 * @see com.fatsoapps.vanitygenerator.core.search.PoolSearch to see usage of QueryPool.
 */
public class QueryPool {

    private Network network;
    private final ArrayList<RegexQuery> queries;
    private GlobalNetParams netParams;

    private static QueryPool instance;

    /**
     * Tries to return the instance of QueryPool. If it doesn't exist, an exception is thrown.
     * @return instance of QueryPool.
     */
    @Nullable
    public static synchronized QueryPool getInstance() {
        return instance;
    }

    /**
     * Creates an instance of QueryPool with a starting Network.
     * @param network - The network to create the QueryPool instance.
     * @param updateNetworkIfExists - Updates the current network if an instance already exists.
     * @return the single instance of QueryPool.
     */
    public static synchronized QueryPool getInstance(Network network, boolean updateNetworkIfExists) {
        if (instance == null) {
            instance = new QueryPool(network);
        } else if (updateNetworkIfExists && instance.network != network) {
            instance.updateNetwork(network);
        }
        return instance;
    }

    private QueryPool(Network network) {
        queries = new ArrayList<RegexQuery>();
        netParams = new GlobalNetParams(network);
        this.network = network;
    }

    private QueryPool(int publicKeyHeader, int p2shHeader, int privateKeyHeader) throws IllegalDecimalVersionException {
        queries = new ArrayList<RegexQuery>();
        netParams = new GlobalNetParams(publicKeyHeader, p2shHeader, privateKeyHeader);
    }

    public synchronized <T extends RegexQuery> void addQuery(T query) {
        if (queries.contains(query)) return;
        queries.add(query);
    }

    public synchronized void removeQuery(int originalHashCode) {
        for (RegexQuery query: queries) {
            if (query.hashCode() == originalHashCode) {
                removeQuery(query);
                break;
            }
        }
    }

    public synchronized <T extends RegexQuery> void removeQuery(T query) {
        if (query == null) return;
        synchronized (queries) {
            queries.remove(query);
        }
    }

    public synchronized <T extends RegexQuery> void updateQuery(T newQuery, int originalHashCode) {
        if (newQuery.hashCode() == originalHashCode || contains(newQuery)) return;
        int index = -1;
        for (int i = 0; i < queries.size(); i++) {
            if (queries.get(i).hashCode() == originalHashCode) {
                index = i;
                break;
            }
        }
        if (index == -1) return;
        queries.set(index, newQuery);
    }

    public <T extends RegexQuery> boolean contains(T query) {
        boolean contains = false;
        for (RegexQuery q: queries) {
            if (q.hashCode() == query.hashCode()) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    public int getAmountOfQueries() {
        return queries.size();
    }

    public boolean containsQueries() {
        return queries.size() > 0;
    }

    /**
     * When a user decides to change the Network they are searching on, all of the queries need to be updated with
     * the new updated Network
     * @param network - Network that is going to replace the old Network.
     */
    public void updateNetwork(Network network) {
        if (this.network != network) {
            this.network = network;
            updateNetwork(new GlobalNetParams(network));
        }
    }

    /**
     * When a user decides to change the GlobalNetParams they are searching on, all of the queries need to be updated
     * with the new GNP. This method is used over updateNetwork(Network) when the networks module is excluded.
     * @param netParams - GNP that is going to replace the old GNP.
     */
    public void updateNetwork(GlobalNetParams netParams) {
        if (this.netParams.getAddressHeader() != netParams.getAddressHeader()) {
            this.netParams = netParams;
            for (RegexQuery query: queries) {
                if (query instanceof NetworkQuery) {
                    ((NetworkQuery) query).updateNetParams(netParams);
                }
            }
        }
    }

    public ArrayList<? extends RegexQuery> getQueries() {
        return queries;
    }

    public Network getNetwork() {
        return network;
    }

    public RegexQuery matches(ECKey key, GlobalNetParams netParams) {
        synchronized (queries) {
            for (RegexQuery query: queries) {
                if (query.matches(key, netParams)) {
                    return query;
                }
            }
        }
        return null;
    }

}
