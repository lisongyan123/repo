private List<ProductImpl> getListOfProductWithProductInfo(List<Long> prodNumList) {
    if (prodNumList == null || prodNumList.isEmpty()) {
        return Collections.emptyList();
    }

    List<ProductImpl> products = new ArrayList<>();
    String hql = """
        SELECT DISTINCT p 
        FROM ProductImpl p 
        LEFT JOIN FETCH p.elnEliStructuredProducts 
        LEFT JOIN FETCH p.languageSupportProducts 
        WHERE p.prodNum IN (:prodNums)
        """;

    for (List<Long> batch : splitList(prodNumList, 1000)) {
        Query query = getSession().createQuery(hql);
        query.setParameterList("prodNums", batch);
        products.addAll(query.list());
    }

    // 初始化 StockInstrumentImpl 的 exchange（避免 lazy load）
    for (ProductImpl p : products) {
        if (p instanceof StockInstrumentImpl stock) {
            Hibernate.initialize(stock.getExchange());
        }
    }

    return products;
}
