package ba.adnan.simplemonriandroidsdkapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Supplier;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.monri.android.Monri;
import com.monri.android.ResultCallback;
import com.monri.android.model.Card;
import com.monri.android.model.ConfirmPaymentParams;
import com.monri.android.model.CustomerParams;
import com.monri.android.model.MonriApiOptions;
import com.monri.android.model.PaymentMethodParams;
import com.monri.android.model.PaymentResult;
import com.monri.android.model.TransactionParams;
import com.monri.android.view.CardMultilineWidget;

import ba.adnan.simplemonriandroidsdkapp.models.NewPaymentResponse;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity implements ResultCallback<PaymentResult> {
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Monri monri;

    private CardMultilineWidget monriCardWidget;
    private Button payButton;
    private TextView result;
    private CheckBox checkbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        setupView();
        bindEvents();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        monri.onPaymentResult(requestCode, data, this);
    }

    private void init() {
        monri = new Monri(
                this.getApplicationContext(),
                MonriApiOptions.create(Config.authenticityToken(), Config.isDevelopmentMode())
        );
    }

    private void setupView() {
        monriCardWidget = findViewById(R.id.monri_card_widget);
        checkbox = findViewById(R.id.checkbox);
        payButton = findViewById(R.id.pay);
        result = findViewById(R.id.result);
    }


    private void bindEvents() {
        payButton.setOnClickListener(view -> proceedWithPayment());
    }

    private void proceedWithPayment() {
        final Card card = monriCardWidget.getCard();
        if (card == null || !card.validateCard()) {
            return;
        }
        final PaymentMethodParams paymentMethodParams = card.setTokenizePan(checkbox.isChecked()).toPaymentMethodParams();

        final Disposable subscription = OrderRepository.createPaymentSession(false)
                .subscribe(handlePaymentSessionResponse(() -> paymentMethodParams));

        compositeDisposable.add(subscription);
    }

    Consumer<NewPaymentResponse> handlePaymentSessionResponse(Supplier<PaymentMethodParams> paymentMethodParamsSupplier) {
        return newPaymentResponse -> {
            if (!"approved".equals(newPaymentResponse.getStatus())) {
                Toast.makeText(this, "Payment session create failed", Toast.LENGTH_LONG).show();
            } else {

                final CustomerParams customerParams = new CustomerParams()
                        .setAddress("Adresa")
                        .setFullName("Tester Testerovic")
                        .setCity("Sarajevo")
                        .setZip("71000")
                        .setPhone("+38761000111")
                        .setCountry("BA")
                        .setEmail("tester+android_sdk@monri.com");

                monri.confirmPayment(this, ConfirmPaymentParams.create(
                        newPaymentResponse.getClientSecret(),
                        paymentMethodParamsSupplier.get(),
                        TransactionParams.create()
                                .set("order_info", "Android SDK payment session")
                                .set(customerParams)
                ));

            }
        };
    }

    @Override
    public void onSuccess(final PaymentResult paymentResult) {
        result.setText(paymentResult.toString());
    }

    @Override
    public void onError(final Throwable throwable) {
        result.setText("Payment failed");
    }
}