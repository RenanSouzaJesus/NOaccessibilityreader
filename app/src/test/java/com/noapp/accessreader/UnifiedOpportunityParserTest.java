package com.noapp.accessreader;

import org.junit.Test;

import static org.junit.Assert.*;

public class UnifiedOpportunityParserTest {

    @Test
    public void parsesIfoodDeliveryFromVisibleText() {
        String raw = "iFood Entregador\nNovo pedido\nR$ 12,90\n"
                + "1,4 km até a retirada\n3,2 km até a entrega\nMcDonald's";

        Opportunity item = UnifiedOpportunityParser.parse(raw, "com.ifood.delivery", "TEST", 1000L);

        assertNotNull(item);
        assertEquals("iFood", item.platform);
        assertEquals(Opportunity.TYPE_DELIVERY, item.type);
        assertEquals(12.90, item.price, 0.001);
        assertEquals(4.6, item.totalKm, 0.001);
        assertEquals(0, item.minutes);
        assertEquals(12.90 / 4.6, item.grossPerKm, 0.001);
        assertEquals(0, item.grossPerHour, 0.001);
    }

    @Test
    public void parsesMercadoLivreRouteWithHours() {
        String raw = "Mercado Livre\nNova rota de entregas\nR$ 285,00\n82 entregas\n"
                + "Estimativa: 6h\nDistância total: 98 km\nTipo: Pacotes e envelopes";

        Opportunity item = UnifiedOpportunityParser.parse(raw, "com.mercadolibre", "TEST", 1000L);

        assertNotNull(item);
        assertEquals("Mercado Livre", item.platform);
        assertEquals(Opportunity.TYPE_ROUTE, item.type);
        assertEquals(98.0, item.totalKm, 0.001);
        assertEquals(360, item.minutes);
        assertEquals(285.0 / 98.0, item.grossPerKm, 0.001);
        assertEquals(47.5, item.grossPerHour, 0.001);
    }

    @Test
    public void parsesShopeeDelivery() {
        String raw = "Shopee Entregas\nNovo pacote\nR$ 9,50\n"
                + "1,1 km até a coleta\n4,8 km até a entrega\nCentro de Distribuição Shopee";

        Opportunity item = UnifiedOpportunityParser.parse(raw, "com.shopee.driver", "TEST", 1000L);

        assertNotNull(item);
        assertEquals("Shopee", item.platform);
        assertEquals(Opportunity.TYPE_DELIVERY, item.type);
        assertEquals(5.9, item.totalKm, 0.001);
        assertEquals(9.50 / 5.9, item.grossPerKm, 0.001);
    }

    @Test
    public void wrapsExistingRideParser() {
        String raw = "Uber Driver\nUberX\nR$ 28,40\n"
                + "3,1 km até o passageiro\n6,2 km • 14 min de viagem";

        Opportunity item = UnifiedOpportunityParser.parse(raw, "com.ubercab.driver", "TEST", 1000L);

        assertNotNull(item);
        assertEquals("Uber", item.platform);
        assertEquals(Opportunity.TYPE_RIDE, item.type);
        assertEquals(9.3, item.totalKm, 0.001);
        assertTrue(item.grossPerHour > 0);
    }
}
