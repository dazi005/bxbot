package com.gazbert.bxbot.core.exchanges;

import com.gazbert.bxbot.core.api.trading.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.junit.Assert.*;

/**
 * <p>
 * Tests the behaviour of the OKCoin Exchange Adapter.
 * </p>
 *
 * <p>
 * Coverage could be better: it does not include calling the
 * {@link OkCoinExchangeAdapter#sendPublicRequestToExchange(String, Map)} and
 * {@link OkCoinExchangeAdapter#sendAuthenticatedRequestToExchange(String, Map)} methods; the code in these methods
 * is a bloody nightmare to test!
 * </p>
 *
 * TODO Unit test {@link OkCoinExchangeAdapter#sendPublicRequestToExchange(String, Map)}  method.
 * TODO Unit test {@link OkCoinExchangeAdapter#sendAuthenticatedRequestToExchange(String, Map)} method.
 *
 * @author gazbert
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.crypto.*")
@PrepareForTest(OkCoinExchangeAdapter.class)
public class TestOkcoinExchangeAdapter {

    // Valid config location - expected on runtime classpath in the ./src/test/resources folder.
    private static final String VALID_CONFIG_LOCATION = "okcoin/okcoin-config.properties";

    // Canned JSON responses from exchange - expected to reside on filesystem relative to project root
    private static final String DEPTH_JSON_RESPONSE = "./src/test/exchange-data/okcoin/depth.json";
    private static final String USERINFO_JSON_RESPONSE = "./src/test/exchange-data/okcoin/userinfo.json";
    private static final String USERINFO_ERROR_JSON_RESPONSE = "./src/test/exchange-data/okcoin/userinfo-error.json";
    private static final String TICKER_JSON_RESPONSE = "./src/test/exchange-data/okcoin/ticker.json";
    private static final String ORDER_INFO_JSON_RESPONSE = "./src/test/exchange-data/okcoin/order_info.json";
    private static final String ORDER_INFO_ERROR_JSON_RESPONSE = "./src/test/exchange-data/okcoin/order_info-error.json";
    private static final String TRADE_BUY_JSON_RESPONSE = "./src/test/exchange-data/okcoin/trade_buy.json";
    private static final String TRADE_SELL_JSON_RESPONSE = "./src/test/exchange-data/okcoin/trade_sell.json";
    private static final String TRADE_ERROR_JSON_RESPONSE = "./src/test/exchange-data/okcoin/trade-error.json";
    private static final String CANCEL_ORDER_JSON_RESPONSE = "./src/test/exchange-data/okcoin/cancel_order.json";
    private static final String CANCEL_ORDER_ERROR_JSON_RESPONSE = "./src/test/exchange-data/okcoin/cancel_order-error.json";

    // Exchange API calls
    private static final String DEPTH = "depth.do";
    private static final String ORDER_INFO = "order_info.do";
    private static final String USERINFO = "userinfo.do";
    private static final String TICKER = "ticker.do";
    private static final String TRADE = "trade.do";
    private static final String CANCEL_ORDER = "cancel_order.do";

    // Canned test data
    private static final String MARKET_ID = "btc_usd";
    private static final BigDecimal BUY_ORDER_PRICE = new BigDecimal("200.18");
    private static final BigDecimal BUY_ORDER_QUANTITY = new BigDecimal("0.01");
    private static final BigDecimal SELL_ORDER_PRICE = new BigDecimal("250.176");
    private static final BigDecimal SELL_ORDER_QUANTITY = new BigDecimal("0.01");
    private static final String ORDER_ID_TO_CANCEL = "99671870";

    // Mocked out methods
    private static final String MOCKED_GET_CONFIG_LOCATION_METHOD = "getConfigFileLocation";
    private static final String MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD = "sendAuthenticatedRequestToExchange";
    private static final String MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD = "sendPublicRequestToExchange";


    // ------------------------------------------------------------------------------------------------
    //  Cancel Order tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testCancelOrderIsSuccessful() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(CANCEL_ORDER_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                OkCoinExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(CANCEL_ORDER),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();

        final boolean success = exchangeAdapter.cancelOrder(ORDER_ID_TO_CANCEL, MARKET_ID);
        assertTrue(success);

        PowerMock.verifyAll();
    }

    @Test
    public void testCancelOrderExchangeErrorResponse() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(CANCEL_ORDER_ERROR_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                OkCoinExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(CANCEL_ORDER),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();
        assertFalse(exchangeAdapter.cancelOrder(ORDER_ID_TO_CANCEL, MARKET_ID));
        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testCancelOrderHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(OkCoinExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(CANCEL_ORDER),
                anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("I’ve thought of an ending for my book – “And he lived happily " +
                        "ever after… to the end of his days."));

        PowerMock.replayAll();
        exchangeAdapter.cancelOrder(ORDER_ID_TO_CANCEL, MARKET_ID);
        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testCancelOrderHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(OkCoinExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(CANCEL_ORDER),
                anyObject(Map.class)).
                andThrow(new IllegalStateException("A Balrog. A demon of the ancient world. This foe is beyond any of" +
                        " you. Run!"));

        PowerMock.replayAll();
        exchangeAdapter.cancelOrder(ORDER_ID_TO_CANCEL, MARKET_ID);
        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Create Orders tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testCreateOrderToBuyIsSuccessful() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(TRADE_BUY_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                OkCoinExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(TRADE),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();

        final String orderId = exchangeAdapter.createOrder(MARKET_ID, OrderType.BUY, BUY_ORDER_QUANTITY, BUY_ORDER_PRICE);
        assertTrue(orderId.equals("99646259"));

        PowerMock.verifyAll();
    }

    @Test
    public void testCreateOrderToSellIsSuccessful() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(TRADE_SELL_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                OkCoinExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(TRADE),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();

        final String orderId = exchangeAdapter.createOrder(MARKET_ID, OrderType.SELL, SELL_ORDER_QUANTITY, SELL_ORDER_PRICE);
        assertTrue(orderId.equals("99646257"));

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testCreateOrderExchangeErrorResponse() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(TRADE_ERROR_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                OkCoinExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(TRADE),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();
        exchangeAdapter.createOrder(MARKET_ID, OrderType.SELL, SELL_ORDER_QUANTITY, SELL_ORDER_PRICE);
        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testCreateOrderHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(OkCoinExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(TRADE),
                anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("It’s like in the great stories, Mr. Frodo, the ones that really " +
                        "mattered. Full of darkness and danger, they were... Those were the stories that stayed" +
                        " with you, that meant something, even if you were too small to understand why. But I think, " +
                        "Mr. Frodo, I do understand... There’s some good in this world, Mr. Frodo, and it’s worth" +
                        " fighting for."));

        PowerMock.replayAll();
        exchangeAdapter.createOrder(MARKET_ID, OrderType.SELL, SELL_ORDER_QUANTITY, SELL_ORDER_PRICE);
        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testCreateOrderHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(OkCoinExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(TRADE),
                anyObject(Map.class)).
                andThrow(new IllegalArgumentException("We needs it. Must have the precious. They stole it from us. " +
                        "Sneaky little hobbitses, wicked, tricksy, false. No, not master... Master’s my friend. " +
                        "You don’t have any friends. Nobody likes you. Not listening. I’m not listening. You’re a liar." +
                        " And a thief. Murderer. Go away... I hate you... Leave now and never come back.”"));

        PowerMock.replayAll();
        exchangeAdapter.createOrder(MARKET_ID, OrderType.BUY, BUY_ORDER_QUANTITY, BUY_ORDER_PRICE);
        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Get Your Open Orders tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingYourOpenOrdersSuccessfully() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(ORDER_INFO_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                OkCoinExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ORDER_INFO),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();

        final List<OpenOrder> openOrders = exchangeAdapter.getYourOpenOrders(MARKET_ID);

        // assert some key stuff; we're not testing GSON here.
        assertTrue(openOrders.size() == 2);
        assertTrue(openOrders.get(0).getMarketId().equals(MARKET_ID));
        assertTrue(openOrders.get(0).getId().equals("99031951"));
        assertTrue(openOrders.get(0).getType() == OrderType.SELL);
        assertTrue(openOrders.get(0).getCreationDate().getTime() == 1442949893000L);
        assertTrue(openOrders.get(0).getPrice().compareTo(new BigDecimal("255")) == 0);
        assertTrue(openOrders.get(0).getQuantity().compareTo(new BigDecimal("0.015")) == 0);
        assertTrue(openOrders.get(0).getTotal().compareTo(openOrders.get(0).getPrice().multiply(openOrders.get(0).getQuantity())) == 0);

        // the values below are not provided by OKCoin
        assertNull(openOrders.get(0).getOriginalQuantity());

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingYourOpenOrdersExchangeErrorResponse() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(ORDER_INFO_ERROR_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                OkCoinExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ORDER_INFO),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();
        exchangeAdapter.getYourOpenOrders("junk_market_id");
        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testGettingYourOpenOrdersHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(OkCoinExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ORDER_INFO),
                anyObject(Map.class)).andThrow(new ExchangeTimeoutException("If more of us valued food and cheer and" +
                " song above hoarded gold, it would be a merrier world."));

        PowerMock.replayAll();

        exchangeAdapter.getYourOpenOrders(MARKET_ID);

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingYourOpenOrdersHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(OkCoinExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ORDER_INFO),
                anyObject(Map.class)).
                andThrow(new IllegalStateException("The Road goes ever on and on\n" +
                        "Down from the door where it began.\n" +
                        "Now far ahead the Road has gone,\n" +
                        "And I must follow, if I can"));

        PowerMock.replayAll();

        exchangeAdapter.getYourOpenOrders(MARKET_ID);

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Get Market Orders tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingMarketOrders() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(DEPTH_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                OkCoinExchangeAdapter.class, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, eq(DEPTH),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();

        final MarketOrderBook marketOrderBook = exchangeAdapter.getMarketOrders(MARKET_ID);

        // assert some key stuff; we're not testing GSON here.
        assertTrue(marketOrderBook.getMarketId().equals(MARKET_ID));

        final BigDecimal buyPrice = new BigDecimal("228.3");
        final BigDecimal buyQuantity = new BigDecimal("52.995");
        final BigDecimal buyTotal = buyPrice.multiply(buyQuantity);

        assertTrue(marketOrderBook.getBuyOrders().size() == 200);
        assertTrue(marketOrderBook.getBuyOrders().get(0).getType() == OrderType.BUY);
        assertTrue(marketOrderBook.getBuyOrders().get(0).getPrice().compareTo(buyPrice) == 0);
        assertTrue(marketOrderBook.getBuyOrders().get(0).getQuantity().compareTo(buyQuantity) == 0);
        assertTrue(marketOrderBook.getBuyOrders().get(0).getTotal().compareTo(buyTotal) == 0);

        final BigDecimal sellPrice = new BigDecimal("228.36");
        final BigDecimal sellQuantity = new BigDecimal("0.01");
        final BigDecimal sellTotal = sellPrice.multiply(sellQuantity);

        assertTrue(marketOrderBook.getSellOrders().size() == 200);
        assertTrue(marketOrderBook.getSellOrders().get(0).getType() == OrderType.SELL);
        assertTrue(marketOrderBook.getSellOrders().get(0).getPrice().compareTo(sellPrice) == 0);
        assertTrue(marketOrderBook.getSellOrders().get(0).getQuantity().compareTo(sellQuantity) == 0);
        assertTrue(marketOrderBook.getSellOrders().get(0).getTotal().compareTo(sellTotal) == 0);

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testGettingMarketOrdersHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(OkCoinExchangeAdapter.class,
                MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, eq(DEPTH),
                anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("All we have to decide is what to do with the time that is given" +
                        " to us."));

        PowerMock.replayAll();
        exchangeAdapter.getMarketOrders(MARKET_ID);
        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingMarketOrdersHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(OkCoinExchangeAdapter.class,
                MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, eq(DEPTH),
                anyObject(Map.class)).
                andThrow(new IllegalArgumentException("The board is set, the pieces are moving. We come to it at last, " +
                        "the great battle of our time."));

        PowerMock.replayAll();
        exchangeAdapter.getMarketOrders(MARKET_ID);
        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Get Latest Market Price tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingLatestMarketPriceSuccessfully() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(TICKER_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                OkCoinExchangeAdapter.class, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, eq(TICKER),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();
        final BigDecimal latestMarketPrice = exchangeAdapter.getLatestMarketPrice(MARKET_ID).setScale(8, BigDecimal.ROUND_HALF_UP);
        assertTrue(latestMarketPrice.compareTo(new BigDecimal("231.35")) == 0);
        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testGettingLatestMarketPriceHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(OkCoinExchangeAdapter.class,
                MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, eq(TICKER),
                anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("I would rather share one lifetime with you than face all the" +
                        " Ages of this world alone."));

        PowerMock.replayAll();
        exchangeAdapter.getLatestMarketPrice(MARKET_ID);
        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingLatestMarketPriceHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(OkCoinExchangeAdapter.class,
                MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, eq(TICKER),
                anyObject(Map.class)).
                andThrow(new IllegalArgumentException("What has happened before will happen again. What has been done " +
                        "before will be done again. There is nothing new in the whole world. \"Look,\" they say, " +
                        "\"here is something new!\" But no, it has all happened before, long before we were born." +
                        " No one remembers what has happened in the past, and no one in days to come will remember what" +
                        " happens between now and then."));

        PowerMock.replayAll();
        exchangeAdapter.getLatestMarketPrice(MARKET_ID);
        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Get Balance Info tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingBalanceInfoSuccessfully() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(USERINFO_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                OkCoinExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(USERINFO),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();

        final BalanceInfo balanceInfo = exchangeAdapter.getBalanceInfo();

        // assert some key stuff; we're not testing GSON here.
        assertTrue(balanceInfo.getBalancesAvailable().get("BTC").compareTo(new BigDecimal("0.06")) == 0);
        assertTrue(balanceInfo.getBalancesAvailable().get("USD").compareTo(new BigDecimal("0.0608")) == 0);

        assertTrue(balanceInfo.getBalancesOnHold().get("BTC").compareTo(new BigDecimal("0.03")) == 0);
        assertTrue(balanceInfo.getBalancesOnHold().get("USD").compareTo(new BigDecimal("2.25")) == 0);

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingBalanceInfoExchangeErrorResponse() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(USERINFO_ERROR_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                OkCoinExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(USERINFO),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();
        exchangeAdapter.getBalanceInfo();
        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testGettingBalanceInfoHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(OkCoinExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(USERINFO),
                eq(null)).andThrow(new ExchangeTimeoutException("There is only one Lord of the Ring, only one who can" +
                " bend it to his will. And he does not share power."));

        PowerMock.replayAll();
        exchangeAdapter.getBalanceInfo();
        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingBalanceInfoHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final OkCoinExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(OkCoinExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(USERINFO), eq(null)).
                andThrow(new IllegalStateException("It's a dangerous business, Frodo, going out your door. You step " +
                        "onto the road, and if you don't keep your feet, there's no knowing where you might be swept " +
                        "off to."));

        PowerMock.replayAll();
        exchangeAdapter.getBalanceInfo();
        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Non Exchange visiting tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingExchangeSellingFeeIsAsExpected() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(VALID_CONFIG_LOCATION);
        PowerMock.replayAll();

        final OkCoinExchangeAdapter exchangeAdapter = new OkCoinExchangeAdapter();
        final BigDecimal sellPercentageFee = exchangeAdapter.getPercentageOfSellOrderTakenForExchangeFee(MARKET_ID);
        assertTrue(sellPercentageFee.compareTo(new BigDecimal("0.002")) == 0);

        PowerMock.verifyAll();
    }

    @Test
    public void testGettingExchangeBuyingFeeIsAsExpected() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(VALID_CONFIG_LOCATION);
        PowerMock.replayAll();

        final OkCoinExchangeAdapter exchangeAdapter = new OkCoinExchangeAdapter();
        final BigDecimal buyPercentageFee = exchangeAdapter.getPercentageOfBuyOrderTakenForExchangeFee(MARKET_ID);
        assertTrue(buyPercentageFee.compareTo(new BigDecimal("0.002")) == 0);

        PowerMock.verifyAll();
    }

    @Test
    public void testGettingImplNameIsAsExpected() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(VALID_CONFIG_LOCATION);
        PowerMock.replayAll();

        final OkCoinExchangeAdapter exchangeAdapter = new OkCoinExchangeAdapter();
        assertTrue(exchangeAdapter.getImplName().equals("OKCoin REST Spot Trading API v1"));

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Initialisation tests - assume config property files are located under src/test/resources
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testExchangeAdapterInitialisesSuccessfully() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(VALID_CONFIG_LOCATION);
        PowerMock.replayAll();

        final OkCoinExchangeAdapter exchangeAdapter = new OkCoinExchangeAdapter();
        assertNotNull(exchangeAdapter);

        PowerMock.verifyAll();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExchangeAdapterThrowsExceptionIfPublicKeyConfigIsMissing() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(
                "okcoin/missing-public-key-okcoin-config.properties");
        PowerMock.replayAll();

        new OkCoinExchangeAdapter();

        PowerMock.verifyAll();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExchangeAdapterThrowsExceptionIfSecretConfigIsMissing() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(
                "okcoin/missing-secret-okcoin-config.properties");
        PowerMock.replayAll();

        new OkCoinExchangeAdapter();

        PowerMock.verifyAll();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExchangeAdapterThrowsExceptionIfTimeoutConfigIsMissing() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(
                "okcoin/missing-timeout-okcoin-config.properties");
        PowerMock.replayAll();

        new OkCoinExchangeAdapter();

        PowerMock.verifyAll();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExchangeAdapterThrowsExceptionIfBuyFeeIsMissing() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(
                "okcoin/missing-buy-fee-okcoin-config.properties");
        PowerMock.replayAll();

        new OkCoinExchangeAdapter();

        PowerMock.verifyAll();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExchangeAdapterThrowsExceptionIfSellFeeIsMissing() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(
                "okcoin/missing-sell-fee-okcoin-config.properties");
        PowerMock.replayAll();

        new OkCoinExchangeAdapter();

        PowerMock.verifyAll();
    }

    /*
     * Used for making real API calls to the exchange in order to grab JSON responses.
     * Have left this in; it might come in useful.
     * It expects VALID_CONFIG_LOCATION to contain the correct credentials.
     */
//    @Test
    public void testCallingExchangeToGetJson() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(OkCoinExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(VALID_CONFIG_LOCATION);
        PowerMock.replayAll();

//        final TradingApi exchangeAdapter = new OkCoinExchangeAdapter();
//        exchangeAdapter.getImplName();
//        exchangeAdapter.getPercentageOfBuyOrderTakenForExchangeFee(MARKET_ID);
//        exchangeAdapter.getPercentageOfSellOrderTakenForExchangeFee(MARKET_ID);
//        exchangeAdapter.getLatestMarketPrice(MARKET_ID);
//        exchangeAdapter.getMarketOrders(MARKET_ID);
//        exchangeAdapter.getYourOpenOrders(MARKET_ID);
//        exchangeAdapter.getBalanceInfo();
//        exchangeAdapter.createOrder(MARKET_ID, OrderType.SELL, SELL_ORDER_QUANTITY, SELL_ORDER_PRICE);
//        exchangeAdapter.cancelOrder(ORDER_ID_TO_CANCEL, MARKET_ID);

        PowerMock.verifyAll();
    }
}