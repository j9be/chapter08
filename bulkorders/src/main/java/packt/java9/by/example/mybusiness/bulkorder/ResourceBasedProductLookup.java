package packt.java9.by.example.mybusiness.bulkorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import packt.java9.by.example.mybusiness.bulkorder.ProductInformation;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class ResourceBasedProductLookup implements ProductLookup {
    private static Logger log = LoggerFactory.getLogger(ResourceBasedProductLookup.class);

    private ProductInformation fromJSON(InputStream jsonStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ProductInformation pi = mapper.readValue(jsonStream,ProductInformation.class);
        return pi;
    }


    final private Map<String, ProductInformation> products = new HashMap<>();
    private boolean productsAreNotLoaded = true;

    private void loadProducts() {
        if (productsAreNotLoaded) {
            try {
                Resource[] resources = new PathMatchingResourcePatternResolver().
                        getResources("classpath:products/*.json");
                for (Resource resource : resources) {
                    loadResource(resource);
                }
                productsAreNotLoaded = false;
            } catch (IOException ex) {
                log.error("Test resources can not be read", ex);
            }
        }
    }

    private void loadResource(Resource resource) throws IOException {
        final int dotPos = resource.getFilename().lastIndexOf('.');
        final String id = resource.getFilename().substring(0, dotPos);
        Properties properties = new Properties();
        properties.load(resource.getInputStream());
        final ProductInformation pi = fromJSON(resource.getInputStream());
        pi.setId(id);
        products.put(id, pi);
    }

    private static final List<String> noProducts = new LinkedList<>();

    @Override
    public ProductInformation byId(String id) {
        loadProducts();
        if (products.containsKey(id)) {
            return products.get(id);
        } else {
            return ProductInformation.emptyProductInformation;
        }
    }

    @Override
    public List<String> byQuery(String query) {
        log.info("query is {}", query);
        loadProducts();
        List<String> pis = new LinkedList<>();
        StringTokenizer st = new StringTokenizer(query, "&=");
        while (st.hasMoreTokens()) {
            final String key = st.nextToken();
            if (st.hasMoreTokens()) {
                final String value = st.nextToken();
                log.debug("processing {}={} query", key, value);
                if (!"title".equals(key)) {
                    log.error("Search by title is allowed only");
                    return noProducts;
                }
                for (String id : products.keySet()) {
                    log.error("key: {} value:{} id:{}", key, value, id);
                    ProductInformation pi = products.get(id);
                    if (pi.getTitle().startsWith(value)) {
                        pis.add(id);
                    }
                }
            }
        }
        return pis;
    }
}