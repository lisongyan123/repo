private List<ProductImpl> getListOfProductWithProductInfo(List<Long> prodNumList) {
    if (prodNumList == null || prodNumList.isEmpty()) return Collections.emptyList();

    StringBuilder hql = new StringBuilder();
    hql.append("SELECT DISTINCT p FROM ProductImpl p ");
    hql.append("LEFT JOIN FETCH p.elnEliStructuredProducts ");
    hql.append("LEFT JOIN FETCH p.languageSupportProducts ");

    // 关键：对 StockInstrumentImpl 的 exchange 做条件 fetch
    // 由于多态，我们只能在查询后处理，或使用 UNION —— 但更简单的方式是：
    // 先查出所有 ProductImpl，然后确保 StockInstrumentImpl 的 exchange 被初始化

    // 更实际的做法：在查询后手动 initialize
    String baseHql = "FROM ProductImpl p WHERE p.prodNum IN (:prodNums)";
    Query query = getSession().createQuery(baseHql);
    
    List<ProductImpl> products = new ArrayList<>();
    for (List<Long> batch : splitList(prodNumList, maxQueryParameterInOracleDb)) {
        query.setParameterList("prodNums", batch);
        products.addAll(query.list());
    }

    // ✅ 手动初始化 exchange（避免 lazy load）
    for (ProductImpl p : products) {
        if (p instanceof StockInstrumentImpl) {
            Hibernate.initialize(((StockInstrumentImpl) p).getExchange());
        }
    }

    return products;
}
