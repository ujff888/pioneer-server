package cn.litgame.wargame.core.logic;

import org.junit.Test;

import com.google.protobuf.InvalidProtocolBufferException;

public class PaymentLogicTest {

	
	//@Test
	public void test() throws InvalidProtocolBufferException{
//		String s = "ewoJInNpZ25hdHVyZSIgPSAiQW5zK09WVkpkay82KzFLaG9zbU5BZXJKZnJqLzlm Z3FIZzFMSnZZeUJzeGdNajFDS2NsSU1hV2xSYmxIOVgvc083aVpVM3l3UTRLemk5 Z1dhOUZFNjhaUW0wd2E1allFNWtPcVJRTGZ3emJ1Z1ppbjAxaFNWS1hETnoxeU96 R2IzdTBsK0xyK1RYeittQmVjNEpIRWZQeE1zU1p6WVpCZm1KMytkeFQvYVZNMEFB QURWekNDQTFNd2dnSTdvQU1DQVFJQ0NCdXA0K1BBaG0vTE1BMEdDU3FHU0liM0RR RUJCUVVBTUg4eEN6QUpCZ05WQkFZVEFsVlRNUk13RVFZRFZRUUtEQXBCY0hCc1pT QkpibU11TVNZd0pBWURWUVFMREIxQmNIQnNaU0JEWlhKMGFXWnBZMkYwYVc5dUlF RjFkR2h2Y21sMGVURXpNREVHQTFVRUF3d3FRWEJ3YkdVZ2FWUjFibVZ6SUZOMGIz SmxJRU5sY25ScFptbGpZWFJwYjI0Z1FYVjBhRzl5YVhSNU1CNFhEVEUwTURZd056 QXdNREl5TVZvWERURTJNRFV4T0RFNE16RXpNRm93WkRFak1DRUdBMVVFQXd3YVVI VnlZMmhoYzJWU1pXTmxhWEIwUTJWeWRHbG1hV05oZEdVeEd6QVpCZ05WQkFzTUVr RndjR3hsSUdsVWRXNWxjeUJUZEc5eVpURVRNQkVHQTFVRUNnd0tRWEJ3YkdVZ1NX NWpMakVMTUFrR0ExVUVCaE1DVlZNd2daOHdEUVlKS29aSWh2Y05BUUVCQlFBRGdZ MEFNSUdKQW9HQkFNbVRFdUxnamltTHdSSnh5MW9FZjBlc1VORFZFSWU2d0Rzbm5h bDE0aE5CdDF2MTk1WDZuOTNZTzdnaTNvclBTdXg5RDU1NFNrTXArU2F5Zzg0bFRj MzYyVXRtWUxwV25iMzRucXlHeDlLQlZUeTVPR1Y0bGpFMU93QytvVG5STStRTFJD bWVOeE1iUFpoUzQ3VCtlWnRERWhWQjl1c2szK0pNMkNvZ2Z3bzdBZ01CQUFHamNq QndNQjBHQTFVZERnUVdCQlNKYUVlTnVxOURmNlpmTjY4RmUrSTJ1MjJzc0RBTUJn TlZIUk1CQWY4RUFqQUFNQjhHQTFVZEl3UVlNQmFBRkRZZDZPS2RndElCR0xVeWF3 N1hRd3VSV0VNNk1BNEdBMVVkRHdFQi93UUVBd0lIZ0RBUUJnb3Foa2lHOTJOa0Jn VUJCQUlGQURBTkJna3Foa2lHOXcwQkFRVUZBQU9DQVFFQWVhSlYyVTUxcnhmY3FB QWU1QzIvZkVXOEtVbDRpTzRsTXV0YTdONlh6UDFwWkl6MU5ra0N0SUl3ZXlOajVV UllISytIalJLU1U5UkxndU5sMG5rZnhxT2JpTWNrd1J1ZEtTcTY5Tkluclp5Q0Q2 NlI0Szc3bmI5bE1UQUJTU1lsc0t0OG9OdGxoZ1IvMWtqU1NSUWNIa3RzRGNTaVFH S01ka1NscDRBeVhmN3ZuSFBCZTR5Q3dZVjJQcFNOMDRrYm9pSjNwQmx4c0d3Vi9a bEwyNk0ydWVZSEtZQ3VYaGRxRnd4VmdtNTJoM29lSk9PdC92WTRFY1FxN2VxSG02 bTAzWjliN1BSellNMktHWEhEbU9Nazd2RHBlTVZsTERQU0dZejErVTNzRHhKemVi U3BiYUptVDdpbXpVS2ZnZ0VZN3h4ZjRjemZIMHlqNXdOelNHVE92UT09IjsKCSJw dXJjaGFzZS1pbmZvIiA9ICJld29KSW05eWFXZHBibUZzTFhCMWNtTm9ZWE5sTFdS aGRHVXRjSE4wSWlBOUlDSXlNREUxTFRBNExURXdJREl4T2pBNU9qSTFJRUZ0WlhK cFkyRXZURzl6WDBGdVoyVnNaWE1pT3dvSkluVnVhWEYxWlMxcFpHVnVkR2xtYVdW eUlpQTlJQ0l6TkRabVltSmhZekJqWmpVeVpqVXpOMlJoT1RJeU1XVXdNamRoTlRS ak5qazNZelppTm1Fd0lqc0tDU0p2Y21sbmFXNWhiQzEwY21GdWMyRmpkR2x2Ymkx cFpDSWdQU0FpTVRBd01EQXdNREUyTnpFd09EazVOQ0k3Q2draVluWnljeUlnUFNB aU1TNHdJanNLQ1NKMGNtRnVjMkZqZEdsdmJpMXBaQ0lnUFNBaU1UQXdNREF3TURF Mk56RXdPRGs1TkNJN0Nna2ljWFZoYm5ScGRIa2lJRDBnSWpFaU93b0pJbTl5YVdk cGJtRnNMWEIxY21Ob1lYTmxMV1JoZEdVdGJYTWlJRDBnSWpFME16a3lOall4TmpV eU1qRWlPd29KSW5WdWFYRjFaUzEyWlc1a2IzSXRhV1JsYm5ScFptbGxjaUlnUFNB aU5qY3hSRVpETUVNdFFqY3hSUzAwTVVVMkxUazRORFF0UkVReFJURTFNVU5FTmps RUlqc0tDU0p3Y205a2RXTjBMV2xrSWlBOUlDSndZWGxmTmw4Mk1DSTdDZ2tpYVhS bGJTMXBaQ0lnUFNBaU1UQXlPREkzTURFNE55STdDZ2tpWW1sa0lpQTlJQ0pqYmk1 c2FYUm5ZVzFsTG10cGJHeGxjaUk3Q2draWNIVnlZMmhoYzJVdFpHRjBaUzF0Y3lJ Z1BTQWlNVFF6T1RJMk5qRTJOVEl5TVNJN0Nna2ljSFZ5WTJoaGMyVXRaR0YwWlNJ Z1BTQWlNakF4TlMwd09DMHhNU0F3TkRvd09Ub3lOU0JGZEdNdlIwMVVJanNLQ1NK d2RYSmphR0Z6WlMxa1lYUmxMWEJ6ZENJZ1BTQWlNakF4TlMwd09DMHhNQ0F5TVRv d09Ub3lOU0JCYldWeWFXTmhMMHh2YzE5QmJtZGxiR1Z6SWpzS0NTSnZjbWxuYVc1 aGJDMXdkWEpqYUdGelpTMWtZWFJsSWlBOUlDSXlNREUxTFRBNExURXhJREEwT2pB NU9qSTFJRVYwWXk5SFRWUWlPd3A5IjsKCSJlbnZpcm9ubWVudCIgPSAiU2FuZGJv eCI7CgkicG9kIiA9ICIxMDAiOwoJInNpZ25pbmctc3RhdHVzIiA9ICIwIjsKfQ==";
//		CSPayment.Builder builder = CSPayment.newBuilder();
//		builder.setOrderId("1");
//		builder.setProductId("p");
//		builder.setReceipt(s);
//		CSPayment payment = builder.build();
//		byte[] data = payment.toByteArray();
//		CSPayment r = CSPayment.parseFrom(data);
//		System.out.println(r);
	}
}
