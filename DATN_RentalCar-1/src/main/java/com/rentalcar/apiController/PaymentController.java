package com.rentalcar.apiController;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rentalcar.util.*;
import com.rentalcar.dao.PaymentRepo;
import com.rentalcar.entity.Account;
import com.rentalcar.entity.Discount;
import com.rentalcar.entity.Feedback;
import com.rentalcar.entity.Payment;
import com.rentalcar.entity.RentalVehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

	@Autowired
	private PaymentRepo paymentRepo;

	private static Map<String, String> stage_zalo_config = new HashMap<String, String>() {
		{
			put("app_id", "2553");
			put("key1", "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL");
			put("key2", "kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz");
			put("endpoint", "https://sb-openapi.zalopay.vn/v2/create");
		}
	};

	public static String getCurrentTimeString(String format) {
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
		SimpleDateFormat fmt = new SimpleDateFormat(format);
		fmt.setCalendar(cal);
		return fmt.format(cal.getTimeInMillis());
	}

	static String createUrlPayment(double totalAmount, String app_trans_id) throws IOException {

		Map<String, Object>[] item = new Map[] { new HashMap<>() {
			{
				put("itemid", "item001");
				put("itemname", "Product A");
				put("itemprice", totalAmount);
				put("itemquantity", 1);
			}
		} };

		Map<String, Object> embed_data = new HashMap<>();
		embed_data.put("merchantinfo", "Test Merchant");
		embed_data.put("redirecturl", "http://localhost:3000/checkout/payment/success");

		Map<String, Object> order = new HashMap<String, Object>() {
			{
				put("app_id", stage_zalo_config.get("app_id"));
				put("app_time", System.currentTimeMillis());
				put("app_trans_id", app_trans_id);
				put("app_user", "datn");
				put("amount", (int) totalAmount);
				put("description", "Payment for the order - DATN DEV TEAM 2025 DEMO");
				put("bank_code", "");
				put("item", new JSONArray(Arrays.asList(item)).toString());
				put("embed_data", new JSONObject(embed_data).toString());
				put("callback_url",
						"https://f5aa-2402-800-629c-f6b2-e84a-d4dd-8856-98bc.ngrok-free.app/api/payment/zalo/callback");
			}
		};

		String data = order.get("app_id") + "|" + order.get("app_trans_id") + "|" + order.get("app_user") + "|"
				+ order.get("amount") + "|" + order.get("app_time") + "|" + order.get("embed_data") + "|"
				+ order.get("item");
		String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, stage_zalo_config.get("key1"), data);
		order.put("mac", mac);

		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(stage_zalo_config.get("endpoint"));

		List<NameValuePair> params = new ArrayList<>();
		for (Map.Entry<String, Object> e : order.entrySet()) {
			params.add(new BasicNameValuePair(e.getKey(), e.getValue().toString()));
		}

		post.setEntity(new UrlEncodedFormEntity(params));

		CloseableHttpResponse res = client.execute(post);

		BufferedReader rd = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
		StringBuilder resultJsonStr = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			resultJsonStr.append(line);
		}

		JSONObject result = new JSONObject(resultJsonStr.toString());

		if (result.has("order_url"))
			return result.getString("order_url");

		return null;
	}

	// Tìm tất cả
	@GetMapping
	public ResponseEntity<List<Payment>> getAll() {
		List<Payment> payments = paymentRepo.findAll();
		if (payments.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(payments);
	}

	// Tìm theo ID
	@GetMapping(value = "/{id}")
	public ResponseEntity<Payment> getById(@PathVariable("id") Long id) {
		Optional<Payment> payment = paymentRepo.findById(id);
		if (payment.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(payment.get());
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public class PaymentResponse {
		private String message;
		private String status;

	}

	@PostMapping
	public ResponseEntity<PaymentResponse> save(@RequestBody Payment payment) {
		try {

			String trans_id = getCurrentTimeString("yyMMdd") + "_" + Integer.parseInt(Helpers.handleRandom(7));

			payment.setTransId(trans_id);

			if ("COD".equals(payment.getPaymentType()) || "prePayment".equals(payment.getPaymentMethod())) {
				payment.setStatus("pending");

				Payment savedPayment = paymentRepo.save(payment);
				return ResponseEntity.status(HttpStatus.CREATED)
						.body(new PaymentResponse("Save payment successfully...", "success"));
			}

			payment.setStatus("unpaid");
			Payment savedPayment = paymentRepo.save(payment);
			String url = createUrlPayment(payment.getAmount().doubleValue(), trans_id);
			
			return ResponseEntity.status(HttpStatus.CREATED).body(new PaymentResponse(url, "success"));

		} catch (Exception e) {
			PaymentResponse response = new PaymentResponse("An error occurred while saving payment.", "error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	// Cập nhật
	@PutMapping(value = "/{id}")
	public ResponseEntity<String> update(@PathVariable("id") Long id, @RequestBody Payment paymentDetail) {
		Optional<Payment> existingPayment = paymentRepo.findById(id);
		if (existingPayment.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment not found");
		}

		Payment paymentUpdate = existingPayment.get();
		paymentUpdate.setAmount(paymentDetail.getAmount());
		paymentUpdate.setRental(paymentDetail.getRental());
		paymentUpdate.setPaymentDate(paymentDetail.getPaymentDate());
		paymentUpdate.setPaymentMethod(paymentDetail.getPaymentMethod());
		paymentUpdate.setNotes(paymentDetail.getNotes());
		paymentUpdate.setIdQrCode(paymentDetail.getIdQrCode());

		paymentRepo.save(paymentUpdate);
		return ResponseEntity.ok("Payment updated successfully");
	}

	// Xóa
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<String> deleteById(@PathVariable("id") Long id) {
		if (!paymentRepo.existsById(id)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment not found");
		}
		paymentRepo.deleteById(id);
		return ResponseEntity.ok("Payment deleted successfully");
	}
}
