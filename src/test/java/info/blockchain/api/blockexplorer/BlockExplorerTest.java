package info.blockchain.api.blockexplorer;

import info.blockchain.api.APIException;
import info.blockchain.api.blockexplorer.entity.*;
import info.blockchain.api.blockexplorer.entity.Address;
import info.blockchain.api.blockexplorer.entity.Transaction;
import info.blockchain.api.pushtx.PushTx;
import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

import static org.bitcoinj.core.Utils.HEX;
import static org.junit.Assert.*;

/**
 * Created by ray on 10/05/2017.
 */
public class BlockExplorerTest {

    BlockExplorer client;

    @Before
    public void setUp () throws Exception {
        client = new BlockExplorer();
    }

    @Test
    public void getAddress () throws Exception {
        Address address = client.getAddress("1jH7K4RJrQBXijtLj1JpzqPRhR7MdFtaW", FilterType.All, 10, null);

        assertEquals("1jH7K4RJrQBXijtLj1JpzqPRhR7MdFtaW", address.getAddress());
        assertEquals("07feead7f9fb7d16a0251421ac9fa090169cc169",
                address.getHash160());
        assertEquals(0, address.getFinalBalance());
        assertEquals(2, address.getTxCount());
        assertEquals(20000, address.getTotalReceived());
        assertEquals(20000, address.getTotalSent());
        assertEquals(2, address.getTransactions().size());
    }

    @Test
    public void getUnspentOutputs () throws Exception {
        String address1 = "1FrWWFJ95Jq7EDgpkeBwVLAtoJMPwmYS7T";
        String address2 = "xpub6CmZamQcHw2TPtbGmJNEvRgfhLwitarvzFn3fBYEEkFTqztus7W7CNbf48Kxuj1bRRBmZPzQocB6qar9ay6buVkQk73ftKE1z4tt9cPHWRn";
        List<UnspentOutput> unspentOutputs = client.getUnspentOutputs(Arrays.asList(address1, address2), 6, 10);

        assertTrue(unspentOutputs != null && unspentOutputs.size() != 0);
        assertEquals("2e7ab41818ee0ab987d393d4c8bf5e436b6e8c15ef3535a2b3eac581e33c7472", unspentOutputs.get(0).getTransactionHash());
        assertEquals(20000, unspentOutputs.get(0).getValue());
    }

    @Test
    public void getBalance () throws Exception {
        String address1 = "1jH7K4RJrQBXijtLj1JpzqPRhR7MdFtaW";
        String address2 = "xpub6CmZamQcHw2TPtbGmJNEvRgfhLwitarvzFn3fBYEEkFTqztus7W7CNbf48Kxuj1bRRBmZPzQocB6qar9ay6buVkQk73ftKE1z4tt9cPHWRn";

        List<String> list = Arrays.asList(address1, address2);

        Map<String, Balance> balances = client.getBalance(list, FilterType.All);

        assertEquals(0, balances.get(address1).getFinalBalance());
        assertEquals(2, balances.get(address1).getTxCount());
        assertEquals(20000, balances.get(address1).getTotalReceived());
        assertEquals(20000, balances.get(address2).getFinalBalance());
        assertEquals(1, balances.get(address2).getTxCount());
        assertEquals(20000, balances.get(address2).getTotalReceived());
    }

    @Test
    public void getMultiAddress () throws Exception {
        String address1 = "1jH7K4RJrQBXijtLj1JpzqPRhR7MdFtaW";
        String address2 = "xpub6CmZamQcHw2TPtbGmJNEvRgfhLwitarvzFn3fBYEEkFTqztus7W7CNbf48Kxuj1bRRBmZPzQocB6qar9ay6buVkQk73ftKE1z4tt9cPHWRn";
        List<String> list = Arrays.asList(address1, address2);
        MultiAddress multiAddress = client.getMultiAddress(list, FilterType.All, null, null);

        //Addresses
        assertEquals("1jH7K4RJrQBXijtLj1JpzqPRhR7MdFtaW", multiAddress.getAddresses().get(0).getAddress());
        assertEquals(2, multiAddress.getAddresses().get(0).getTxCount());
        assertEquals(20000, multiAddress.getAddresses().get(0).getTotalReceived());
        assertEquals(20000, multiAddress.getAddresses().get(0).getTotalSent());
        assertEquals(0, multiAddress.getAddresses().get(0).getFinalBalance());

        assertEquals("xpub6CmZamQcHw2TPtbGmJNEvRgfhLwitarvzFn3fBYEEkFTqztus7W7CNbf48Kxuj1bRRBmZPzQocB6qar9ay6buVkQk73ftKE1z4tt9cPHWRn", multiAddress
                        .getAddresses().get(1).getAddress());
        assertEquals(1, multiAddress.getAddresses().get(1).getTxCount());
        assertEquals(20000, multiAddress.getAddresses().get(1).getTotalReceived());
        assertEquals(0, multiAddress.getAddresses().get(1).getTotalSent());
        assertEquals(20000, multiAddress.getAddresses().get(1).getFinalBalance());
        assertEquals(0, multiAddress.getAddresses().get(1).getChangeIndex());
        assertEquals(1, multiAddress.getAddresses().get(1).getAccountIndex());
        assertEquals(20, multiAddress.getAddresses().get(1).getGapLimit());
    }

    @Test
    public void getXpub () throws Exception {
        String address = "xpub6CmZamQcHw2TPtbGmJNEvRgfhLwitarvzFn3fBYEEkFTqztus7W7CNbf48Kxuj1bRRBmZPzQocB6qar9ay6buVkQk73ftKE1z4tt9cPHWRn";
        XpubFull xpub = client.getXpub(address, null, null, null);

        assertEquals(xpub.getAddress(),
                "xpub6CmZamQcHw2TPtbGmJNEvRgfhLwitarvzFn3fBYEEkFTqztus7W7CNbf48Kxuj1bRRBmZPzQocB6qar9ay6buVkQk73ftKE1z4tt9cPHWRn");
        assertEquals(1, xpub.getTxCount());
        assertEquals(20000, xpub.getTotalReceived());
        assertEquals(0, xpub.getTotalSent());
        assertEquals(20000, xpub.getFinalBalance());
        assertEquals(0, xpub.getChangeIndex());
        assertEquals(1, xpub.getAccountIndex());
        assertEquals(20, xpub.getGapLimit());
    }

    @Test
    public void testCreateAddress(){
        NetworkParameters parameters = new TestNet3Params();
        ECKey ecKey = new ECKey(new SecureRandom());

        String wif = ecKey.getPrivateKeyAsWiF(parameters);
        System.out.println("wif=" + wif);


//        org.bitcoinj.core.Address address = org.bitcoinj.core.Address.fromKey(parameters,ecKey, Script.ScriptType.P2PKH);
//        System.out.println("address=" + address.toString());
    }

    @Test
    public void createRawTransaction2() throws IOException, APIException {
        String address = "1Gmrt9JrhCqrE5uyeAFDVSg9jkumKhBcZS";
//        String priKey = "cSDpY36p44cJwQBLS3SwcTGyFzaTWwVL9nFVfa7cibdbYZ2dNJoM";
        String priKey = "L1MH1cQXgpgvU9d9x4d4s1J6mkSnY1CnSpcm7Jg4cru8oJG3ubpP";
        String toAddress = "12bU9ZLmJfEU35q8HfZTY97ihkmi2Mb1qL";
        long value = 5000;
        long fee = 1000;
        String hex = createRawTransaction(address,toAddress,priKey,address,value,fee);
        //Transaction transaction = blockCypherContext.getTransactionService().sendRawTransaction(hex);
        //System.out.println("tx = " + GsonFactory.getGson().toJson(transaction));
    }

    private String createRawTransaction(String from,String to,String priKey,String change,long value , long fee) throws APIException, IOException {

        NetworkParameters params = MainNetParams.get();

        DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(null, priKey);
        ECKey ecKey = dpk.getKey();

        //校验私钥
        String tmpAdr = org.bitcoinj.core.Address.fromKey(params,ecKey, Script.ScriptType.P2PKH).toString();
        if(!from.equals(tmpAdr)){
            throw new RuntimeException("私钥不正确");
        }

        BlockExplorer blockExplorer = new BlockExplorer();
        List<UnspentOutput> unspentOutputs = blockExplorer.getUnspentOutputs(from);

        org.bitcoinj.core.Transaction tx = new org.bitcoinj.core.Transaction(params);

        if(unspentOutputs != null){
            //排序，先消耗最小的
            Collections.sort(unspentOutputs, new Comparator<UnspentOutput>() {
                public int compare(UnspentOutput o1, UnspentOutput o2) {
                    long div = o1.getValue() - o2.getValue();
                    if(div == 0){
                        return 0;
                    }else if(div < 0){
                        return -1;
                    }else{
                        return 1;
                    }
                }
            });

            List<UnspentOutput> inputSummary = new ArrayList<UnspentOutput>();

            long sum = 0;
            for (UnspentOutput summary : unspentOutputs){
                if(sum < (value + fee)){
                    sum += summary.getValue();
                    inputSummary.add(summary);
                }else{
                    break;
                }
            }
            if(sum < value + fee){
                throw new RuntimeException("余额不足");
            }

            //计算找零，如果不指定找零剩余的金额会全部算作矿工费
            long changeNum = sum - (value + fee);
            //设置目标地址
            tx.addOutput(Coin.valueOf(value), org.bitcoinj.core.Address.fromString(params,to));
            //设置找零地址
            tx.addOutput(Coin.valueOf(changeNum), org.bitcoinj.core.Address.fromString(params,change));

            for(UnspentOutput summary : inputSummary){
                TransactionOutPoint txopt = new TransactionOutPoint(params,summary.getN(),
                        Sha256Hash.wrap(summary.getTransactionHash()));
                tx.addSignedInput(txopt,new Script(HEX.decode(summary.getScript())),ecKey,
                        org.bitcoinj.core.Transaction.SigHash.ALL, true);
            }

            String hexTx = HEX.encode(tx.bitcoinSerialize());
            System.out.format("raw tx => %s\n",hexTx);

            return hexTx;
        }

        return null;
    }

    @Test
    public void testPushTX() throws APIException, IOException {
        String result = PushTx.pushTx("0100000001802e3bca063acf2cf6acb36d54e5c2465b0b912621a5e02b0861d936670ac69d010000006b483045022100a50822e351d1a73d6987f7531438f417ed930fc3d26519ce4d38deddee22eeb802206ea47d2aabcf9cb7546253aad2e06e784850c645883fb7a5464e4ef990c0a8c78121033c9df7de414d742b01a3272f8263ee89b7aba49cc76301f780e69f3607332985ffffffff0288130000000000001976a914117cee1befffd5110991f5533f22faa3bbdd362d88ac46c00200000000001976a914ad05d40ab5368e771e0976843e33dec4c3d172fb88ac00000000");
        System.out.println("tx=" + result);
    }


    @Test
    public void testTxHash(){
        String hash = "320d2b3cb26723b26321c2ea17bfaf448222a3138352d6b1c2426375a76bc214";
        System.out.println("hex=" + HEX.encode(hash.getBytes()));
    }

    @Test
    public void testGetTransaction() throws APIException, IOException {
        Address address = client.getAddress("1Gmrt9JrhCqrE5uyeAFDVSg9jkumKhBcZS", FilterType.All, 10, null);
        List<Transaction> txs = address.getTransactions();
        if(txs != null){
            for(Transaction tx : txs){
                System.out.println(tx.toString());
            }
        }
    }

}