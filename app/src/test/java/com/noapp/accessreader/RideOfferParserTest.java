package com.noapp.accessreader;

import org.junit.Test;

import static org.junit.Assert.*;

public class RideOfferParserTest {

    @Test
    public void parsesUberSimulatorOffer() {
        String raw = "TEXT: Uber\n" +
                "TEXT: UberX\n" +
                "TEXT: R$ 28,40\n" +
                "TEXT: 3,1 km até o passageiro\n" +
                "TEXT: 6,2 km • 14 min de viagem\n";

        RideOfferParser.RideOffer offer = RideOfferParser.parse(raw);

        assertNotNull(offer);
        assertEquals("Uber", offer.platform);
        assertEquals("UberX", offer.category);
        assertEquals(28.40, offer.price, 0.001);
        assertEquals(3.1, offer.pickupKm, 0.001);
        assertEquals(6.2, offer.tripKm, 0.001);
        assertEquals(14, offer.tripMinutes);
        assertEquals(9.3, offer.totalKm, 0.001);
        assertEquals(28.40 / 9.3, offer.grossPerKm, 0.001);
        assertEquals("BOA", offer.rating);
    }

    @Test
    public void parses99SimulatorOffer() {
        String raw = "TEXT: Simulação - 99\n" +
                "TEXT: 99Pop\n" +
                "TEXT: R$ 31,20\n" +
                "DESC: Distância até o passageiro: 2,4 quilômetros\n" +
                "DESC: Viagem: 7,0 quilômetros e 16 minutos\n";

        RideOfferParser.RideOffer offer = RideOfferParser.parse(raw);

        assertNotNull(offer);
        assertEquals("99", offer.platform);
        assertEquals("99Pop", offer.category);
        assertEquals(31.20, offer.price, 0.001);
        assertEquals(2.4, offer.pickupKm, 0.001);
        assertEquals(7.0, offer.tripKm, 0.001);
        assertEquals(16, offer.tripMinutes);
    }

    @Test
    public void ignoresLauncherNoise() {
        String raw = "TEXT: NO\nDESC: Simulador de Corridas Botão\nTEXT: Limpar\nVIEW_ID: com.android.launcher:id/page_indicator";
        assertNull(RideOfferParser.parse(raw));
    }

    @Test
    public void requiresCompleteOffer() {
        String raw = "TEXT: UberX\nTEXT: R$ 18,00\nTEXT: 5,0 km";
        assertNull(RideOfferParser.parse(raw));
    }
}
