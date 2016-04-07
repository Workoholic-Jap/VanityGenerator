package co.bitsquared.vanitygenerator.core.search;

/**
 * Searching for multiple queries can have its advantages if it is incorporated right. Blindly searching for every
 * single query in a collection will have a major drawback on speed and will ultimately slow down performance. This is
 * meant to serve as an efficiency booster when searching and is ultimately up to the programmer to choose which fits
 * best.
 */
public enum SearchMode {

    /**
     * Search for every query provided at once. This will have the worst performance in regards to other search modes and
     * will only get worse when there are more queries to search on.
     */
    SEARCH_ALL,

    /**
     * Search for one query at a time in terms of difficulty from easiest to hardest. Due to the semantics of this search mode,
     * findUnlimited setting of each query will be ignored meaning that when a query is found, it will be removed from the collection.
     */
    EASIEST_HARDEST,

    /**
     * Search for one query at a time in terms of difficulty from hardest to easiest. Due to the semantics of this search mode,
     * findUnlimited setting of each query will be ignored meaning that when a query is found, it will be removed from the collection.
     */
    HARDEST_EASIEST;

}
