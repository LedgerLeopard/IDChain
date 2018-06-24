package com.ledgerleopard.sorvin;

import android.text.TextUtils;

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
                return Did.keyForLocalDid(wallet, did).get();
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

	public CompletableFuture<Void> connectMyDidWithForeignDid(String myDid, String foreignDid ){
		return CompletableFuture.supplyAsync(() -> {
            try {
                // check if foreign key already stored in wallet
                if ( !checkIfTheirDidAlreadyStored(foreignDid) ){
                    Did.storeTheirDid(wallet, String.format("{\"did\":\"%s\"}", foreignDid)).get();
                }

                if ( !Pairwise.isPairwiseExists(wallet, foreignDid).get() ){
                    Pairwise.createPairwise(wallet, foreignDid, myDid, GOVERNMENT_CONNECTION_NAME).get();
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

            return null;
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


                    ConnectionItem[] connectionItemsAr = gson.fromJson(sb.toString(), ConnectionItem[].class);
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
				return Crypto.authCrypt(wallet, senderVerKey, receiverVerKey, message).get();
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
    public CompletableFuture<AnoncredsResults.ProverCreateCredentialRequestResult> createOfferRequest(String credDefId){
		return getGovernmentMyDid().thenApplyAsync(connectionItem -> {
            try {
                String credentialOffer = Anoncreds.issuerCreateCredentialOffer(wallet, credDefId).get();
                String masterSecret = Anoncreds.proverCreateMasterSecret(wallet, null).get();
                return Anoncreds.proverCreateCredentialReq(wallet, connectionItem.myId, credentialOffer, credDefId, masterSecret).get();
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
