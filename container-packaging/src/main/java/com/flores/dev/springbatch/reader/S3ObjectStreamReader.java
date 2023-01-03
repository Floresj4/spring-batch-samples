package com.flores.dev.springbatch.reader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.core.io.InputStreamResource;

import com.flores.dev.springbatch.model.Person;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

/**
 * I had to create a builder manually because lombok doesn't
 * let you hide fields
 * 
 * @author jason
 */
@Slf4j
public class S3ObjectStreamReader implements ItemStreamReader<Person> {

	private String resource;
	
	/**
	 * The client is managed by Spring.  Do not close the
	 * client when closing the resource
	 */
	private S3Client client;

	private ItemStreamReader<Person> delegate;

	private ResponseInputStream<GetObjectResponse> response;

	private final Lock lock = new ReentrantLock();

	private S3ObjectStreamReader(S3ObjectStreamReaderBuilder builder) {
		this.resource = builder.resource;
		this.client = builder.client;
	}

	@PostConstruct
	public void init() throws URISyntaxException {
		delegate = getDelegate();
	}

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		delegate.open(executionContext);
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		delegate.update(executionContext);
	}

	@Override
	public void close() throws ItemStreamException {
		//close the response before closing the entire reader
		try { response.close(); } catch (IOException e) {
			log.error("Unable to close response on StreamReader.close()");
		}

		delegate.close();
		delegate = null;
	}

	/**
	 * get the GetObjectRequest from a resource path
	 * @param path uri string
	 * @return a S3 GetObjectRequest
	 * @throws URISyntaxException on path errors
	 */
	public static GetObjectRequest getObjectRequestFromResourcePath(String path) throws URISyntaxException {
		URI uri = new URI(path);
		String bucket = uri.getHost();
		String key = uri.getPath().substring(1);

		log.debug("S3 GetObjectRequest bucket and key from {} as {} : {}",
				path, bucket, key);

		return GetObjectRequest.builder()
				.bucket(bucket)
				.key(key)
				.build();
	}

	/**
	 * Fetch the S3 Object input stream and set on JsonItemReader
	 * @return an ItemStream reader from the remote object input stream
	 * @throws URISyntaxException on bad path
	 */
	private ItemStreamReader<Person> getDelegate() throws URISyntaxException {
		if(Optional.ofNullable(delegate).isEmpty()) {			
			log.info("Initializing remote resource reader for {}", resource);

			//request object
			GetObjectRequest request = getObjectRequestFromResourcePath(resource);
			response = client.getObject(request);

			delegate = new FlatFileItemReaderBuilder<Person>()
					.resource(new InputStreamResource(response))
					.name("StreamedPersonReader")
					.delimited()
					.names(new String[] {"firstName", "lastName"})
					.targetType(Person.class)
					.build();
		}
		
		return delegate;
	}
	
	@Override
	public Person read() throws Exception {
		lock.lock();
		
		try {
			delegate = getDelegate();
			return delegate.read();
		}
		finally {
			lock.unlock();
		}
	}
	
	public static S3ObjectStreamReaderBuilder builder() {
		return new S3ObjectStreamReaderBuilder();
	}

	public static class S3ObjectStreamReaderBuilder {

		private String resource;
		
		private S3Client client;
		
		S3ObjectStreamReaderBuilder() {
			
		}
		
		public S3ObjectStreamReaderBuilder withResource(String resource) {
			this.resource = resource;
			return this;
		}
		
		public S3ObjectStreamReaderBuilder withClient(S3Client client) {
			this.client = client;
			return this;
		}
		
		public S3ObjectStreamReader build() {
			return new S3ObjectStreamReader(this);
		}
	}
}