package com.elgin.elginexperience;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.elgin.elginexperience.shipay.ShipayClient;
import com.elgin.elginexperience.shipay.models.Buyer;
import com.elgin.elginexperience.shipay.models.OrderItem;
import com.elgin.elginexperience.shipay.models.wallet.Wallet;
import com.elgin.elginexperience.shipay.requests.AuthenticationRequest;
import com.elgin.elginexperience.shipay.requests.CreateOrderRequest;
import com.elgin.elginexperience.shipay.responses.AuthenticationResponse;
import com.elgin.elginexperience.shipay.responses.CancelOrderResponse;
import com.elgin.elginexperience.shipay.responses.CreateOrderResponse;
import com.elgin.elginexperience.shipay.responses.GetOrderStatusResponse;
import com.elgin.elginexperience.shipay.responses.GetWalletsResponse;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShipayMenu extends AppCompatActivity {
    Context context;
    static Printer printer;
    Button buttonShipayOption;

    // Campo VALOR
    EditText editTextValueShipay;

    Button buttonEnviarTransacao;
    Button buttonCancelarTransacao;
    Button buttonStatusVenda;

    View responseContainer;

    TextView textRetorno;
    TextView textValorVenda;
    TextView textDataVenda;
    TextView textStatusVenda;
    TextView textCarteiraVenda;

    ImageView imgQrCode;

    ShipayClient shipayClient;
    String accessToken;
    String orderId;

    String formattedDate = "";
    String valor = "";
    String wallet = "";
    String status = "";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_shipay_menu);

        this.shipayClient = new ShipayClient();
        printer = new Printer(this);
        printer.printerInternalImpStart();

        authenticate();

        buttonShipayOption = findViewById(R.id.buttonShipayOption);

        editTextValueShipay = findViewById(R.id.editTextInputValueShipay);

        buttonEnviarTransacao = findViewById(R.id.buttonEnviarTransacao);
        buttonCancelarTransacao = findViewById(R.id.buttonCancelarTransacao);
        buttonStatusVenda = findViewById(R.id.buttonStatusVenda);

        responseContainer =  findViewById(R.id.responseContainer);

        textRetorno = findViewById(R.id.textRetorno);
        textValorVenda = findViewById(R.id.textValorVenda);
        textDataVenda = findViewById(R.id.textDataVenda);
        textStatusVenda = findViewById(R.id.textStatusVenda);
        textCarteiraVenda = findViewById(R.id.textCarteiraVenda);

        imgQrCode = findViewById(R.id.imgQrCode);

        buttonEnviarTransacao.setOnClickListener(v -> createOrder());

        buttonCancelarTransacao.setOnClickListener(v -> cancelOrder());

        buttonStatusVenda.setOnClickListener(v -> getOrderStatus());
    }


    public void authenticate() {
        String accessToken = "HV8R8xc28hQbX4fq_jaK1A";
        String secretKey = "ZBD0yR5ybNuHPKqvH0YEiL-hXzfsd4mbot5NuZQ75ZqpMFVuTN__mkFnbl7E6QbXYhVlohnBQ7GQaoLckrrmAA";
        String clientId = "8HMB1egUeKI-h9s4I3gU_w1R6kYifrUfZRrauhvjvX9y2bVoBdpoH7vVm3FZOfFejKB-rEIRjVHBEQxrW93iE40ljPwcVEgfZnKN5SvObHxHvXrgfg87A7aUOeWroajczHNt6KUOwB4-YH90RidhzIJhQ0GEjKwpQt93XJeC1XE";
        AuthenticationRequest authData = new AuthenticationRequest(accessToken, secretKey, clientId);
        shipayClient.getApiService().authenticate(authData).enqueue(new Callback<AuthenticationResponse>() {
            public void onFailure(@NotNull Call call, @NotNull Throwable t) {
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) {
                AuthenticationResponse authResponse = (AuthenticationResponse) response.body();
                if (authResponse != null) {
                    Log.i("ACCESS_TOKEN", authResponse.access_token);
                    Log.i("REFRESH_TOKEN", authResponse.refresh_token);
                    ShipayMenu.this.accessToken = String.format("Bearer %s", authResponse.access_token);
                }
            }
        });
    }

    public void createOrder() {
        Map<String, Object> mapValues = new HashMap<>();
        String orderRef = "shipaypag-stg-005";
        String wallet = "shipay-pagador";
        float total = Float.parseFloat(editTextValueShipay.getText().toString());

        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("Cerveja Heineken", total, 1));

        Buyer buyer = new Buyer("Shipay", "PDV", "000.000.000-00", "shipaypagador@shipay.com.br", "+55 11 99999-9999");
        CreateOrderRequest orderRequest = new CreateOrderRequest(orderRef, wallet, total, items, buyer);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String formattedDate = formatter.format(new Date());
        shipayClient.getApiService().createOrder(accessToken, orderRequest).enqueue(new Callback<CreateOrderResponse>() {
            public void onFailure(@NotNull Call call, @NotNull Throwable t) {
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) {
                CreateOrderResponse orderResponse = (CreateOrderResponse) response.body();
                if (orderResponse != null) {
                    Log.i("deep_link", orderResponse.deep_link);
                    Log.i("order_id", orderResponse.order_id);
                    Log.i("pix_dict_key", "" + orderResponse.pix_dict_key);
                    Log.i("pix_psp", "" + orderResponse.pix_psp);
                    Log.i("qr_code", orderResponse.qr_code);
                    Log.i("qr_code_text", orderResponse.qr_code_text);
                    Log.i("status", orderResponse.status);
                    Log.i("wallet", orderResponse.wallet);

                    // Load Qr Code
                    String cleanImage = orderResponse.qr_code.replace("data:image/png;base64,", "").replace("data:image/jpeg;base64,", "");
                    byte[] decodedString = Base64.decode(cleanImage, Base64.DEFAULT);
                    Bitmap bitMap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imgQrCode.setImageBitmap(bitMap);

                    textValorVenda.setText(String.format("Valor:\t\t\t\t\t\t\t\t\t\t%s", "R$ " + editTextValueShipay.getText().toString()));
                    textDataVenda.setText(String.format("Data:\t\t\t\t\t\t\t\t\t\t\t%s", formattedDate));
                    textStatusVenda.setText(String.format("Status:\t\t\t\t\t\t\t\t\t%s", getFormattedStatus(orderResponse.status)));
                    textCarteiraVenda.setText(String.format("Carteira:\t\t\t\t\t\t\t\t%s", orderResponse.wallet));

                    ShipayMenu.this.orderId = orderResponse.order_id;
                    ShipayMenu.this.valor = "R$ " + editTextValueShipay.getText().toString();
                    ShipayMenu.this.wallet = orderResponse.wallet;
                    ShipayMenu.this.formattedDate = formattedDate;
                    ShipayMenu.this.status = getFormattedStatus(orderResponse.status);

                    responseContainer.setVisibility(View.VISIBLE);

                    mapValues.put("quant", 10);
                    mapValues.put("base64", cleanImage);

                    printer.imprimeCupomTEF(mapValues);
                    printer.AvancaLinhas(mapValues);
                    printer.cutPaper(mapValues);
                }
            }
        });
    }

    public void getWallets() {
        shipayClient.getApiService().getWallets(accessToken).enqueue(new Callback<GetWalletsResponse>() {
            public void onFailure(@NotNull Call call, @NotNull Throwable t) {
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) {
                GetWalletsResponse walletsResponse = (GetWalletsResponse) response.body();
                if (walletsResponse != null) {
                    List<Wallet> wallets = walletsResponse.wallets;
                    for (Wallet wallet :
                            wallets) {
                        Log.i("WALLET", wallet.name);
                    }
                }
            }
        });
    }

    public void cancelOrder() {
        new AlertDialog.Builder(context)
                .setTitle("Cancelar transação")
                .setMessage("Tem certeza de que quer cancelar a transação?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    shipayClient.getApiService().cancelOrder(accessToken, orderId).enqueue(new Callback<CancelOrderResponse>() {
                        public void onFailure(@NotNull Call call, @NotNull Throwable t) {
                        }

                        public void onResponse(@NotNull Call call, @NotNull Response response) {
                            CancelOrderResponse orderResponse = (CancelOrderResponse) response.body();
                            Log.i("tok", response.toString());
                            if (orderResponse != null) {
                                Log.i("order_id", orderResponse.order_id);
                                Log.i("status", getFormattedStatus(orderResponse.status));

                                textStatusVenda.setText(String.format("Status:\t\t\t\t\t\t\t\t\t%s", getFormattedStatus(orderResponse.status)));
                            }
                        }
                    });
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void getOrderStatus() {
        shipayClient.getApiService().getOrderStatus(accessToken, orderId).enqueue(new Callback<GetOrderStatusResponse>() {
            public void onFailure(@NotNull Call call, @NotNull Throwable t) {
            }

            public void onResponse(@NotNull Call call, @NotNull Response response) {
                GetOrderStatusResponse orderResponse = (GetOrderStatusResponse) response.body();
                Log.i("tok", response.toString());
                if (orderResponse != null) {
                    Log.i("created_at", orderResponse.created_at);
                    Log.i("external_id", orderResponse.external_id);
                    Log.i("order_id", orderResponse.order_id);
                    Log.i("paid_amount", String.valueOf(orderResponse.paid_amount));
                    Log.i("payment_date", " " + orderResponse.payment_date);
                    Log.i("pix_psp", " " + orderResponse.pix_psp);
                    Log.i("status", orderResponse.status);
                    Log.i("total_order", String.valueOf(orderResponse.total_order));
                    Log.i("updated_at", orderResponse.updated_at);
                    Log.i("wallet", orderResponse.wallet);
                    Log.i("wallet_payment_id", " " + orderResponse.wallet_payment_id);
                    textDataVenda.setText(String.format("Data:\t\t\t\t\t\t\t\t\t\t\t%s", formattedDate));
                    textStatusVenda.setText(String.format("Status:\t\t\t\t\t\t\t\t\t%s", getFormattedStatus(orderResponse.status)));
                }
            }
        });
    }

    public String getFormattedStatus(String status) {
        switch (status) {
            case "approved":
                return "Aprovado";
            case "expired":
                return "Expirado";
            case "cancelled":
                return "Cancelado";
            case "refunded":
                return "Devolvido";
            case "pending":
                return "Pendente";
            default:
                return "Desconhecido";
        }
    }
}