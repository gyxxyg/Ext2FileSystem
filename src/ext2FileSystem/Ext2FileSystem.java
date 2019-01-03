package ext2FileSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class Ext2FileSystem {
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		FileSystem fileSystem = new FileSystem();
		/*File file = new File("test.txt");
		RandomAccessFile test = new RandomAccessFile(file, "rw");
		test.seek(1);
		test.write(110);
		System.out.println(test.readInt());*/
	}

}

class SuperBlock {
	public int inodes_count;
	public int blocks_count;
	//public int r_blocks_count;
	public int free_blocks_count;
	public int free_inodes_count;
	public int first_data_block;
	public int log_block_size;
	public int blocks_per_group;
	public int inodes_per_group;
	public int mtime;
	public int wtime;
	public int magic;
	public int state;
	public int first_inode_index;
	public int inode_size;
	public int block_group_nr;
	public int[] content;
	public int[] byte_length;
	
	public SuperBlock() {
		content = new int[15];
		this.inodes_count = 65527;	content[0] = inodes_count;
		this.blocks_count = 262144;	content[1] = blocks_count;
		this.free_blocks_count = 256143;	content[2] = free_blocks_count;
		this.free_inodes_count = 65526;	content[3] = free_inodes_count;
		this.first_data_block = 1;	content[4] = first_data_block;
		this.log_block_size = 1024;	content[5] = log_block_size;
		this.blocks_per_group = 262143;	content[6] = blocks_per_group;
		this.inodes_per_group = 65526;	content[7] = inodes_per_group;
		this.mtime = this.wtime = (int) (System.currentTimeMillis()/1000);
		content[8] = content[9] = mtime;
		this.magic = 1;	content[10] = magic;
		this.state = 1;	content[11] = state;
		this.first_inode_index = 0;	content[12] = first_inode_index;
		this.inode_size = 93;	content[13] = inode_size;
		this.block_group_nr = 0;	content[14] = block_group_nr;
		this.byte_length = new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 2, 2, 4, 2, 2};
	}
}

// ����������
class GDT {
	public int block_bitmap;
	public int inode_bitmap;
	public int inode_table;
	public int free_blocks_count;
	public int free_inodes_count;
	public int used_dirs_count;
	public int[] content;
	public int[] byte_length;
	
	public GDT() {
		content = new int[6];
		block_bitmap = 3;	content[0] = block_bitmap;
		inode_bitmap = 35;	content[1] = inode_bitmap;
		inode_table = 43;	content[2] = inode_table;
		free_blocks_count = 256143;	content[3] = free_blocks_count;
		free_inodes_count = 65527;	content[4] = free_inodes_count;
		used_dirs_count = 1;	content[5] = used_dirs_count;
		byte_length = new int[]{4, 4, 4, 2, 2, 2};
	}
}

// Ŀ¼��
class DirItem {
	public int inode;
	public int name_len;
	public int file_type;
	public int[] name;
	
	public DirItem() {
		inode = 0;
		name_len = 0;
		name_len = 0;
		file_type = 0;
		name = new int[13];
		for(int i = 0; i < 13; i++) {
			name[i] = 0;
		}
	}
}

// i�ڵ�
class INode {
	int mode;
	int size;
	int atime;
	int ctime;
	int mtime;
	int dtime;
	int links_count;
	int blocks;
	int[] block;
	
	public INode() {
		mode = 0;
		size = 0;
		atime = ctime = mtime = (int) (System.currentTimeMillis()/1000);
		dtime = 0;
		links_count = 0;
		blocks = 0;
		block = new int[15];
		for(int i = 0; i < 15; i++) {
			block[i] = 0;
		}
	}
}

class FileSystem {
	public  File extFile;
	public RandomAccessFile ext;
	public SuperBlock superBlock;
	public GDT[] gdt;
	
	// ��ʼ���ļ�ϵͳ
	public FileSystem() throws IOException {
		extFile = new File("ext2.bin");
		ext = new RandomAccessFile(extFile, "rw");
		superBlock = new SuperBlock();
		int gdt_count = 1;
		gdt = new GDT[gdt_count];
		for(int i = 0; i < gdt_count; i++) {
			gdt[i] = new GDT();
		}
		createFileSystem(1);
	}
	
	// ������numberת��Ϊlength���ȵ�byte����
	private byte[] intToByte(int number, int length) {  
        int temp = number;  
        byte[] result = new byte[length];  
        for (int i = 0; i < length; i++) {  
        	result[i] = new Integer(temp & 0xff).byteValue();// �����λ���������λ  
            temp = temp >> 8;// ������8λ  
        }  
        return result;  
    }
	
	 /*public static int byteToInt(byte[] b) {  
	        int s = 0;  
	        int s0 = b[0] & 0xff;// ���λ  
	        int s1 = b[1] & 0xff;  
	        int s2 = b[2] & 0xff;  
	        int s3 = b[3] & 0xff;  
	        s3 <<= 24;  
	        s2 <<= 16;  
	        s1 <<= 8;  
	        s = s0 | s1 | s2 | s3;  
	        return s;  
	    }  */
	
	// ��ʼ��size�ֽ�������
	private void createBootBlock(int size) throws IOException {
		byte[] buffer = new byte[size];
		for(int i = 0; i < size; i++) {
			buffer[i] = 0;
		}
		ext.write(buffer);
	}
	
	// ��ʼ��size�ֽڵĳ�����
	private void createSuperBlock(int size) throws IOException {
		for(int i = 0; i < superBlock.byte_length.length; i++) {
			byte[] buffer = intToByte(superBlock.content[i], superBlock.byte_length[i]);
			ext.seek(ext.length()); 
			ext.write(buffer);
		}
		// ������0����
		int data_length = 0;
		for(int i = 0; i < superBlock.byte_length.length; i++) {
			data_length += superBlock.byte_length[i];
		}
		int zero_length = size - data_length;
		byte[] buffer = new byte[zero_length];
		for(int i = 0; i < zero_length; i++) {
			buffer[i] = 0;
		}
		ext.seek(ext.length()); 
		ext.write(buffer);
	}
	
	// ��ʼ��size�ֽڵĿ����������飬��count������
	private void createGDT(int size, int count) throws IOException {
		for(int i = 0; i < count; i++) {
			GDT current_gdt = gdt[i];
			for(int j = 0; j < current_gdt.byte_length.length; j++) {
				byte[] buffer = intToByte(current_gdt.content[j], current_gdt.byte_length[j]);
				ext.seek(ext.length());
				ext.write(buffer);
			}
		}
		// ������0����
		int data_length = 0;
		for(int i = 0; i < gdt[0].byte_length.length; i++) {
			data_length += gdt[0].byte_length[i];
		}
		int zero_length = size - data_length*count;
		byte[] buffer = new byte[zero_length];
		for(int i = 0; i < zero_length; i++) {
			buffer[i] = 0;
		}
		ext.seek(ext.length()); 
		ext.write(buffer);
	}
	
	// �ڿ�λͼ�н�idΪindex�Ŀ��λ����Ϊʹ��
	private void setBlockUsed(int index) throws IOException {
		int base_length = gdt[0].block_bitmap*1024;
		int out_index = index / 8;
		int in_index = index % 8;
		int start_length = base_length + out_index;
		ext.seek(start_length);
		int bits = ext.read();
		int set = 1 << (7-in_index);
		bits = bits | set;
		byte[] new_bits = intToByte(bits, 1);
		ext.write(new_bits);
	}
	
	// ��i�ڵ�λͼ�н�idΪindex��i�ڵ��λ����Ϊʹ��
	private void setInodeUsed(int index) throws IOException {
		int base_length = gdt[0].inode_bitmap*1024;
		int out_index = index / 8;
		int in_index = index % 8;
		int start_length = base_length + out_index;
		ext.seek(start_length);
		int bits = ext.read();
		int set = 1 << (7-in_index);
		bits = bits | set;
		byte[] new_bits = intToByte(bits, 1);
		ext.write(new_bits);
	}
	
	// ��ʼ������Ϊsize�Ŀ�λͼ
	private void createBlockBitmap(int size) throws IOException {
		// ��ʼ��λͼΪ0
		byte[] buffer = new byte[size];
		for(int i = 0; i < size; i++) {
			buffer[i] = 0;
		}
		ext.seek(ext.length());
		ext.write(buffer);
		// ����idΪ1�Ŀ鱻ռ��
		setBlockUsed(1);
	}
	
	// ��ʼ������Ϊsize��i�ڵ�λͼ
	private void createInodeBitmap(int size) throws IOException {
		// ��ʼ��λͼΪ0
		byte[] buffer = new byte[size];
		for(int i = 0; i < size; i++) {
			buffer[i] = 0;
		}
		ext.seek(ext.length());
		ext.write(buffer);
		// ����idΪ0��i�ڵ㱻ռ��
		setInodeUsed(0);
	}
	
	// ��i�ڵ�nodeת��Ϊ�ֽ�������ʽ
	private byte[] inodeToByte(INode node) {
		byte[] result = new byte[88];
		ArrayList<byte[]> temp = new ArrayList<byte[]>();
		temp.add(intToByte(node.mode, 2));
		temp.add(intToByte(node.size, 4));
		temp.add(intToByte(node.atime, 4));
		temp.add(intToByte(node.ctime, 4));
		temp.add(intToByte(node.mtime, 4));
		temp.add(intToByte(node.dtime, 4));
		temp.add(intToByte(node.links_count, 2));
		temp.add(intToByte(node.blocks, 4));
		for(int i = 0; i < 15; i++) {
			temp.add(intToByte(node.block[i], 4));
		}
		int k = 0;
		for(int i = 0; i < temp.size(); i++) {
			for(int j = 0; j < temp.get(i).length; j++) {
				result[k++] = temp.get(i)[k];
			}
		}
		return result;
	}
	
	// ����idΪinode_idֵΪnode��i�ڵ�
	private void insertInode(int inode_id, INode node) throws IOException {
		int base_length = gdt[0].inode_table * 1024;
		int block_index = inode_id / 11;
		int in_index = inode_id % 11;
		int length = base_length + block_index*1024 + in_index*93;
		byte[] buffer = inodeToByte(node);
		ext.seek(length);
		ext.write(buffer);
	}
	
	// ��ʼ������Ϊsize��i�ڵ��
	private void createInodeTable(int size) throws IOException {
		// ��ʼ��i�ڵ��
		byte[] buffer = new byte[size];
		for(int i = 0; i < size; i++) {
			buffer[i] = 0;
		}
		ext.seek(ext.length());
		ext.write(buffer);
		// ����idΪ0��i�ڵ�
		INode new_node = new INode();
		new_node.mode = 1;
		new_node.blocks = 1;
		new_node.block[0] = 1;
		insertInode(0, new_node);
	}
	
	// ��ʼ������Ϊsize��������
	public void createDataBlock(int size) throws IOException {
		// ��ʼ��
		int blocks = size / 1024;
		for(int i = 0; i < blocks; i++) {
			byte[] buffer = new byte[1024];
			for(int j = 0; j < 1024; j++) {
				buffer[j] = 0;
			}
			ext.seek(ext.length());
			ext.write(buffer);
		}
		// ��ʼ����Ŀ¼
	}
	
	// ��ʼ���ļ�ϵͳ
	public void createFileSystem(int section_size) throws IOException {
		createBootBlock(1024);
		createSuperBlock(1024);
		createGDT(1024, 1);
		createBlockBitmap(32*1024);
		createInodeBitmap(8*1024);
		createInodeTable(5957*1024);
		createDataBlock(256144*1024);
	}
	
	// ����ʼ���Ϊstart_index���ļ���insert_positionλ�ò���size�ֽڵ�buffer����
	public boolean insertToFile(int start_index, int insert_position, int size, byte[] buffer) {
		return false;
	}
		
	// ����ʼ���Ϊstart_index��Ŀ¼���Ŀ¼��item
	public boolean insertToDir(int start_index, DirItem item) {
		return false;
	}
	
	// ������ʼ���Ϊdir_index��Ŀ¼����Ϊname���ļ���Ŀ¼����ʼ���ݿ��
	public int getStartBlockByName(int dir_index, String name) {
		return 0;
	}
	
	// ������ʼ���Ϊdir_index��Ŀ¼�ĵ�item_index��Ŀ¼�����ʼ���ݿ��
	public int getStartBlockByIndex(int dir_index, int item_index) {
		return 0;
	}
	
	// ������ʼ���Ϊdir_index��Ŀ¼����Ϊname���ļ���Ŀ¼����i�ڵ�����
	public INode getINodeByName(int dir_index, String name) {
		return new INode();
	}
	
	// ������ʼ���Ϊdir_index��Ŀ¼�ĵ�item_index�ļ���i�ڵ����� 
	public INode getINodeByIndex(int dir_index, int item_index) {
		return new INode();
	}
	
	// ������ʼ���Ϊdir_index���ļ���startλ�ÿ�ʼ��size���ֽ�
	public byte[] getFileContent(int dir_index, int start, int size) {
		byte[] result = null;
		return result;
	}
	
	// ���i��i�ڵ����size���µĿ��block
	public boolean insertBlockIntoInode(int i, int size, int[] block) {
		return false;
	}
	
	// ɾ����i��i�ڵ��еĵ�block_index��
	public boolean deleteBlockFromInode(int i, int block_index) {
		return false;
	}
		
	// ����n�����п�Ŀ��
	public int[] getNFreeBlock(int n) {
		int[] result = null;
		return result;
	}
}
