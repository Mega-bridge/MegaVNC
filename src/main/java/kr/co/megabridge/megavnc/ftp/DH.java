package kr.co.megabridge.megavnc.ftp;



public class DH {


	public DH(long generator, long modulus) throws Exception {
		maxNum = (((long) 1) << DH_MAX_BITS) - 1;
		if (generator >= maxNum || modulus >= maxNum)
			throw new Exception("Modulus or generator too large.");
		gen = generator;
		mod = modulus;
	}

	private long rng(long limit) {
		return (long) (Math.random() * limit);
	}



	private long XpowYmodN(long x, long y, long N) {
		long result = 1;
		final long oneShift63 = ((long) 1) << 63;
		
		for (int i = 0; i < 64; y <<= 1, i++){
			result = result * result % N;
			if ((y & oneShift63) != 0)
				result = result * x % N;
		}
		return result;
	}
	

	public long createInterKey() {
		priv = rng(maxNum);
		return pub = XpowYmodN(gen,priv,mod);
	}
	
	public long createEncryptionKey(long interKey) throws Exception {
		if (interKey >= maxNum){
			throw new Exception("interKey too large");
		}
		return key = XpowYmodN(interKey,priv,mod);
	}
	

	
	public static byte[] longToBytes(long number) {
		byte[] bytes  = new byte[8];
		for (int i = 0; i < 8; i++) {
			bytes[i] = (byte) (0xff & (number >> (8 * (7 - i))));
		}
		return bytes;
	}
	

	private long gen;
	private long mod;
	private long priv;
	private long pub;
	private long key;
	private long maxNum;

	private static final int DH_MAX_BITS = 31;


}