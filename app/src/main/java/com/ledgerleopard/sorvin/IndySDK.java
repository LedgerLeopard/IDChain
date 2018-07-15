package com.ledgerleopard.sorvin;

import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.ledgerleopard.sorvin.model.ConnectionItem;
import com.ledgerleopard.sorvin.model.SchemaDefinition;
import com.sun.jna.Library;
import com.sun.jna.Native;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.crypto.Crypto;
import org.hyperledger.indy.sdk.crypto.CryptoResults;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.ledger.LedgerResults;
import org.hyperledger.indy.sdk.pairwise.Pairwise;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.pool.PoolJSONParameters;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class IndySDK implements Library {

	protected static final String POOL = "docker-pool";
	protected static final String WALLET = "Wallet5";
	protected static final String TYPE = "default";
	protected static final String GOVERNMENT_CONNECTION_NAME = "Government connection";
	protected static final String credentials = "{\"key\": \"\"}";
	protected static final String TAG = IndySDK.class.getCanonicalName();


	private static IndySDK instance;
	private Wallet wallet;
	private final Gson gson;
	private Pool pool;


	public static IndySDK getInstance() {
		if (instance == null)
			instance = new IndySDK();

		return instance;
	}

	public IndySDK() {
		System.loadLibrary("crypto");
		System.loadLibrary("ssl");
		System.loadLibrary("sodium");
		System.loadLibrary("zmq");
		System.loadLibrary("indy");

		LibIndy.init();
		if (!LibIndy.isInitialized()) {
			new RuntimeException("LIb indy not initialized!");
		}

		gson = new Gson();
	}

	public void setStoragePath(String path) {
		// to support new functions to update environment variables
		NewFuncsInstance.INSTANCE.set_indy_home(path);

		String indy_home = NewFuncsInstance.INSTANCE.get_indy_home();

		if (TextUtils.isEmpty(indy_home) || path.compareToIgnoreCase(indy_home) == 0)
			new RuntimeException("Indy home not set successfully. Library will not work");
	}

	public String getStoragePath(){
		return NewFuncsInstance.INSTANCE.get_indy_home();
	}


	private interface NewFuncsInstance extends Library {
		NewFuncsInstance INSTANCE = (NewFuncsInstance) Native.loadLibrary("indy", NewFuncsInstance.class);

		void set_indy_home(String string);
		String get_indy_home();
	}


	// *****************************************************************************************************************
	// todo - credentials filed -  { "key": "wallet pass phrase" } -
	// todo - when register feature will be added we can ask user to provide pass phrase
	// todo - and use it as a parameter there
	public void createAndOpenWallet() {
		try {
			Wallet.createWallet(POOL, WALLET, TYPE, null, credentials).get();
			wallet = Wallet.openWallet(WALLET, null, credentials).get();
		} catch (IndyException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public void closeWallet() {
		try {
			if (wallet != null) {
				wallet.closeWallet().get();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (IndyException e) {
			e.printStackTrace();
		}
	}

	public boolean isWalletExist() {
		try {
			wallet = Wallet.openWallet(WALLET, null, credentials).get();
			return true;
		} catch (IndyException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return false;
	}


	// *********************************************************************************************
	// DID
	public CompletableFuture<ConnectionItem> getGovernmentMyDid( ) {
		return CompletableFuture.supplyAsync(() -> {
            try {
                List<ConnectionItem> connectionItems = IndySDK.getInstance().getConnectionsList().get();
                for (ConnectionItem connectionItem : connectionItems) {
                    if ( connectionItem.connectionName.compareToIgnoreCase(GOVERNMENT_CONNECTION_NAME) == 0) {
                        return connectionItem;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }

            return null;
        });
	}


	public CompletableFuture<String> getVerKeyForDidLocal( String did ) {
		return CompletableFuture.supplyAsync(() -> {
            try {
	            String res = Did.keyForLocalDid(wallet, did).get();
	            return res;
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } catch (IndyException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        });
	}

	public CompletableFuture<String> getVerKeyForDidFromLedger( String did ) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				String res = Did.keyForDid(pool, wallet, did).get();
				return res;
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (IndyException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		});
	}

	public CompletableFuture<DidResults.CreateAndStoreMyDidResult> createAndStoreMyDid( ) {
		return CompletableFuture.supplyAsync(() -> {
            try {
                // create my did
                return Did.createAndStoreMyDid(wallet, "{}").get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } catch (IndyException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        });
	}

	public CompletableFuture<Boolean> connectMyDidWithForeignDid(String myDid, String theirDid, String theirVerKey ){
		return CompletableFuture.supplyAsync(() -> {
            try {
                // check if foreign key already stored in wallet
                if ( !checkIfTheirDidAlreadyStored(theirDid) ){
                    Did.storeTheirDid(wallet, String.format("{\"did\":\"%s\",\"verkey\":\"%s\"}", theirDid, theirVerKey)).get();
                }

                if ( !Pairwise.isPairwiseExists(wallet, theirDid).get() ){
                    Pairwise.createPairwise(wallet, theirDid, myDid, GOVERNMENT_CONNECTION_NAME).get();
                }

                // just to verify that we have put verkey and did
	            String savedVerKey = Did.keyForLocalDid(wallet, theirDid).get();
                return savedVerKey.equals(theirVerKey);


            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } catch (IndyException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        });
	}

	/**
	 * I have not found functions in indy SDK that will allow to check if the foreign key already stored in the wallet
	 * But... every foreign/my key right now will be stored as a connection
	 * Also but... I know the output of the Pairwise.listPairwise(wallet).get() = "{"my_did":"EP51uAgmBThsnbch6Na5NZ","their_did":"XCuwXpFwTP4aeALhn3DADJ","metadata":"Name"}
	 * That means I can check only existans this key in this json output
	 * @param theirDid
	 * @return
	 */
	private boolean checkIfTheirDidAlreadyStored( String theirDid ) {
		try {
			String listPairwise = Pairwise.listPairwise(wallet).get();
			return listPairwise.contains(theirDid);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (IndyException e) {
			e.printStackTrace();
		}

		return false;
	}


	private boolean checkIfIHaveAlreadyCreatedMyDid() {
		try {
			String listPairwise = Pairwise.listPairwise(wallet).get();
			return listPairwise.contains("my_did");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (IndyException e) {
			e.printStackTrace();
		}

		return false;
	}

	public CompletableFuture<List<ConnectionItem>> getConnectionsList( ){
		return CompletableFuture.supplyAsync(() -> {
            try {
                String resString = Pairwise.listPairwise(wallet).get();
                if (resString.compareToIgnoreCase("[]") != 0){
                    resString = resString.replace("\\", "");
                    StringBuilder sb = new StringBuilder(resString);
	                sb.deleteCharAt(sb.indexOf("\""));
	                sb.deleteCharAt(sb.lastIndexOf("\""));

                    String result = sb.toString().replace("}\",\"{", "},{");

                    ConnectionItem[] connectionItemsAr = gson.fromJson(result, ConnectionItem[].class);
                    return Arrays.asList(connectionItemsAr);
                } else {
                    return new ArrayList<>();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } catch (IndyException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        });
	}



	// *********************************************************************************************
	// ENCRYPTION
	public CompletableFuture<byte[]> encrypt( String verKey, String message ) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return Crypto.anonCrypt(verKey, message.getBytes()).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (IndyException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		});
	}

	public CompletableFuture<byte[]> decrypt( String verKey, byte[] message ) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return Crypto.anonDecrypt(wallet, verKey, message).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (IndyException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		});
	}

	public CompletableFuture<byte[]> decryptAuth( String verKey, byte[] message ) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				CryptoResults.AuthDecryptResult authDecryptResult = Crypto.authDecrypt(wallet, verKey, message).get();
				return authDecryptResult.getDecryptedMessage();

			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (IndyException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		});
	}

	public CompletableFuture<byte[]> encryptAuth( String senderVerKey, String receiverVerKey, byte[] message ) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				String originalMessage = new String(message, Charset.forName("utf-8"));

				byte[] bytes = Crypto.authCrypt(wallet, senderVerKey, receiverVerKey, message).get();
				Log.e(TAG, "");
				return bytes;
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (IndyException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		});
	}

	public CompletableFuture<byte[]> sign( String verKey, String message ) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return Crypto.cryptoSign(wallet, verKey, message.getBytes() ).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (IndyException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		});
	}


	public interface IndyCallback<T> {
		void onDone( T result, String errorMessage );
	}

	// *********************************************************************************************
	// POOOOOOOL
	public CompletableFuture<Void> createAndOpenPoolFromConfigFile(File configFile){
		return CompletableFuture.supplyAsync(() -> {
            try {
                String fileName = configFile.getName();
                PoolJSONParameters.CreatePoolLedgerConfigJSONParameter createPoolLedgerConfigJSONParameter
                        = new PoolJSONParameters.CreatePoolLedgerConfigJSONParameter(configFile.getAbsolutePath());
                Pool.createPoolLedgerConfig(fileName, createPoolLedgerConfigJSONParameter.toJson()).get();
				pool = Pool.openPoolLedger(fileName, null).get();
			}  catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } catch (IndyException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
            return null;
        });
	}

	public CompletableFuture<Void> deletePoolConfig(String fileName){
		return CompletableFuture.supplyAsync(() -> {
			try {
				Pool.deletePoolLedgerConfig(fileName).get();

			}  catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (IndyException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
			return null;
		});
	}

    // *********************************************************************************************
    // REQUEST
    public CompletableFuture<CreateOfferRequestResult> createOfferRequest(String credDefId, String credentialOffer){
		return getGovernmentMyDid().thenApplyAsync(connectionItem -> {
            try {
	            // get detailed information about credDefId from ledger
            	String credDefRequest = Ledger.buildGetCredDefRequest(connectionItem.myId, credDefId).get();
	            String response = Ledger.submitRequest(pool, credDefRequest).get();
	            LedgerResults.ParseResponseResult credDefIdResponse = Ledger.parseGetCredDefResponse(response).get();

	            // making request
                String masterSecret = Anoncreds.proverCreateMasterSecret(wallet, null).get();
	            AnoncredsResults.ProverCreateCredentialRequestResult proverCreateCredentialRequestResult = Anoncreds.proverCreateCredentialReq(wallet, connectionItem.myId, credentialOffer, credDefIdResponse.getObjectJson(), masterSecret).get();
	            return new CreateOfferRequestResult(credDefIdResponse, proverCreateCredentialRequestResult);
            } catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (IndyException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
        });
    }

    public class CreateOfferRequestResult {
	    public LedgerResults.ParseResponseResult credDef;
	    public AnoncredsResults.ProverCreateCredentialRequestResult proverCreateCredentialResult;

	    public CreateOfferRequestResult(LedgerResults.ParseResponseResult credDef, AnoncredsResults.ProverCreateCredentialRequestResult proverCreateCredentialResult) {
		    this.credDef = credDef;
		    this.proverCreateCredentialResult = proverCreateCredentialResult;
	    }
    }

	public CompletableFuture<String> storeCredentials(String credentialName, String credReqMetadataJson, String credJson, String credDefJson ){
		return CompletableFuture.supplyAsync(() -> {
			try {
				String credentialID = Anoncreds.proverStoreCredential(wallet, credentialName, credReqMetadataJson, credJson, credDefJson, null).get();
				return credentialID;
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			} catch (IndyException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		});
	}

	public CompletableFuture<String> getAllCredentials() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				String result = Anoncreds.proverGetCredentials(wallet, "{}").get();
				return result;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (IndyException e) {
				e.printStackTrace();
			}
			return null;
		});
	}

	public CompletableFuture<String> getCredentialsForProofRequest( String proofRequest ) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				//String inner = "{\"nonce\":\"123432421212\",\"name\":\"Loan-Application-Basic\",\"version\":\"0.1\",\"requested_attributes\":{\"attr1_referent\":{\"name\":\"givenname@http://schema.org/text\"},\"attr2_referent\":{\"name\":\"surname@http://schema.org/text\",\"restrictions\":[{\"cred_def_id\":\"7d1trfrU7BbE3pZ85hMZGX:3:CL:30\"}]},\"attr3_referent\":{\"name\":\"bsn@https://nl.wikipedia.org/wiki/burgerservicenummer\"}},\"requested_predicates\":{}}";
				//String innerWithUnknownAttribute = "{\"nonce\":\"123432421212\",\"name\":\"Loan-Application-Basic\",\"version\":\"0.1\",\"requested_attributes\":{\"attr1_referent\":{\"name\":\"givenname@http://schema.org/text\"},\"attr2_referent\":{\"name\":\"surname@http://schema.org/text\",\"restrictions\":[{\"cred_def_id\":\"7d1trfrU7BbE3pZ85hMZGX:3:CL:30\"}]},\"attr3_referent\":{\"name\":\"bsn@https://nl.wikipedia.org/wiki/burgerservicenummer\"},\"attr4_referent\":{\"name\":\"CUSTOM_ATTRIBUTE\"}},\"requested_predicates\":{}}";

				String result = Anoncreds.proverGetCredentialsForProofReq(wallet, proofRequest).get();
				return result;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (IndyException e) {
				e.printStackTrace();
			}
			return null;
		});
	}

	// *********************************************************************************************
	// LEDGER
	public CompletableFuture<SchemaDefinition> getSchemaAttributes(String schemaId){
		return getGovernmentMyDid().thenApplyAsync(connectionItem -> {
            try {
                String schemaRequest = Ledger.buildGetSchemaRequest(connectionItem.myId, schemaId).get();
				String response = Ledger.submitRequest(pool, schemaRequest).get();
                LedgerResults.ParseResponseResult parseResponseResult = Ledger.parseGetSchemaResponse(response).get();
				return gson.fromJson(parseResponseResult.getObjectJson(), SchemaDefinition.class);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } catch (IndyException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        });
	}
}
