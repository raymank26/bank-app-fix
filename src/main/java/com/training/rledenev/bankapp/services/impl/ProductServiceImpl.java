package com.training.rledenev.bankapp.services.impl;

import com.training.rledenev.bankapp.dto.AgreementDto;
import com.training.rledenev.bankapp.entity.Product;
import com.training.rledenev.bankapp.entity.enums.CurrencyCode;
import com.training.rledenev.bankapp.entity.enums.ProductType;
import com.training.rledenev.bankapp.exceptions.ProductNotFoundException;
import com.training.rledenev.bankapp.exceptions.RequestApiException;
import com.training.rledenev.bankapp.repository.ProductRepository;
import com.training.rledenev.bankapp.services.ProductService;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import static com.training.rledenev.bankapp.services.impl.ServiceUtils.getEnumName;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    @Override
    public List<Product> getAllActiveProducts() {
        return productRepository.findAllActiveProducts();
    }

    @Transactional
    @Override
    public List<Product> getActiveProductsWithType(String productType) {
        return productRepository.findAllActiveProductsWithType(ProductType.valueOf(getEnumName(productType)));
    }

    @Transactional
    @Override
    public Product getSuitableProduct(AgreementDto agreementDto) {
        BigDecimal convertedAmount = BigDecimal.valueOf(agreementDto.getSum());
        if (!agreementDto.getCurrencyCode().equals(CurrencyCode.PLN.toString())) {
            BigDecimal rate = getRateOfCurrency(agreementDto.getCurrencyCode());
            convertedAmount = convertedAmount.multiply(rate);
        }
        return productRepository.getProductByTypeSumAndPeriod(getEnumName(agreementDto.getProductType()),
                convertedAmount.doubleValue(), agreementDto.getPeriodMonths())
                .orElseThrow(() -> new ProductNotFoundException("Amount or period is out of limit"));
    }

    @Override
    public BigDecimal getRateOfCurrency(String currencyCode) {
        JSONObject currencyJson;
        try {
            currencyJson = getCurrencyJsonObject(currencyCode);
        } catch (IOException e) {
            throw new RequestApiException(e.getMessage());
        }
        JSONObject subObject = currencyJson.getJSONArray("rates").getJSONObject(0);
        return BigDecimal.valueOf(subObject.getDouble("mid"));
    }

    @NotNull
    private static JSONObject getCurrencyJsonObject(String message) throws IOException {
        URL url = new URL("http://api.nbp.pl/api/exchangerates/rates/A/" + message);
        Scanner scanner = new Scanner((InputStream) url.getContent());
        StringBuilder result = new StringBuilder();
        while (scanner.hasNext()) {
            result.append(scanner.nextLine());
        }
        return new JSONObject(result.toString());
    }
}
