package com.etz.gh.kyc.server;

import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.Methods;

/**
 *
 * @author seth.sebeh
 */
public class RouteHandler {

    public static final RoutingHandler ROUTES() {
        RoutingHandler routes = new RoutingHandler();
        routes.add(Methods.GET, "/direct", new MomoKYCHandler());
        routes.add(Methods.GET, "/gcbverify", new GIPKYCHandler());
        routes.add(Methods.POST, "/process/gcbropay/payment", new BlockingHandler(new GcbRopayPaymentHandler()));
        routes.add(Methods.POST, "/process/service/pay/notify", new BlockingHandler(new GcbServicePayNotifyHandler()));
        routes.add(Methods.POST, "/process/policeservice/pay/notify", new BlockingHandler(new GcbPoliceServicePayNotifyHandler()));
        routes.add(Methods.POST, "/gcb/ais/service/pay", new BlockingHandler(new AISServiceHandler()));

        routes.add(Methods.GET, "/echo", new EchoService());
        routes.setFallbackHandler(new FallbackHandler());
        return routes;
    }

}
