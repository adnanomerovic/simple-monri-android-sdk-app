package ba.adnan.simplemonriandroidsdkapp;

import ba.adnan.simplemonriandroidsdkapp.models.NewPaymentRequest;
import ba.adnan.simplemonriandroidsdkapp.models.NewPaymentResponse;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

interface ExampleApi {
    @POST("example/create-payment-session")
    Single<NewPaymentResponse> createPaymentSession(@Body NewPaymentRequest request);
}
