package ext2FileSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class Ext2FileSystem {
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//FileSystem fileSystem = new FileSystem();
		File file = new File("test.txt");
		RandomAccessFile test = new RandomAccessFile(file, "rw");
		System.out.println(test.read());
		System.out.println(test.read());
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
		this.first_inode_index = 1;	content[12] = first_inode_index;
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
	public byte[] intToByte(int number, int length) {  
        int temp = number;  
        byte[] result = new byte[length];  
        for (int i = 0; i < length; i++) {  
        	result[i] = new Integer(temp & 0xff).byteValue();// �����λ���������λ  
            temp = temp >> 8;// ������8λ  
        }  
        return result;  
    }
	
	// ��byte�����start��ʼ��length����λת��Ϊint
	public int byteToInt(byte[] bytes, int start, int length) {  
		int result = 0;
		int[] temp = new int[length];
		for(int i = 0; i < length; i++) {
			temp[i] = bytes[i+start] & 0xff;
			temp[i] <<= 8*i;
			result |= temp[i];
		}
	    return result;  
	}
	
	// ��ʼ��size�ֽ�������
	public void createBootBlock(int size) throws IOException {
		byte[] buffer = new byte[size];
		for(int i = 0; i < size; i++) {
			buffer[i] = 0;
		}
		ext.write(buffer);
	}
	
	// ��ʼ��size�ֽڵĳ�����
	public void createSuperBlock(int size) throws IOException {
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
	public void createGDT(int size, int count) throws IOException {
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
	public void setBlockUsed(int index) throws IOException {
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
	public void setInodeUsed(int index) throws IOException {
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
	public void createBlockBitmap(int size) throws IOException {
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
	public void createInodeBitmap(int size) throws IOException {
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
	public byte[] inodeToByte(INode node) {
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
				result[k++] = temp.get(i)[j];
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
		// ����idΪ1��i�ڵ�
		INode new_node = new INode();
		new_node.mode = 1;
		new_node.size = 16*2;
		new_node.blocks = 1;
		new_node.block[0] = 1;
		insertInode(1, new_node);
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
		DirItem first_item = new DirItem();
		DirItem second_item = new DirItem();
		first_item.inode = 1;
		first_item.name_len = 1;
		first_item.file_type = 1;
		first_item.name[0] = '.';
		second_item.inode = 1;
		second_item.name_len = 2;
		second_item.file_type = 1;
		second_item.name[0] = second_item.name[1] = '.';
		insertToDir(1, first_item);
		insertToDir(1, second_item);
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
	
	// ���ֽ���ת��Ϊi�ڵ�node
	private INode byteToNode(byte[] buffer) {
		int[] byte_length = new int[]{2, 4, 4, 4, 4, 4, 2, 4};
		int[] temp = new int[byte_length.length];
		int begin = 0;
		for(int i = 0; i < byte_length.length; i++) {
			temp[i] = byteToInt(buffer, begin, byte_length[i]);
			begin += byte_length[i];
		}
		int[] temp_block = new int[15];
		for(int i = 0; i < 15; i++) {
			temp_block[i] = byteToInt(buffer, begin, 4);
			begin += 4;
		}
		INode node = new INode();
		node.mode = temp[0];
		node.size = temp[1];
		node.atime = temp[2];
		node.ctime = temp[3];
		node.mtime = temp[4];
		node.dtime = temp[5];
		node.links_count = temp[6];
		node.blocks = temp[7];
		for(int i = 0; i < 15; i++) {
			node.block[i] = temp_block[i];
		}
		return node;
	}
	
	// ����idΪinode_index��i�ڵ�
	public INode getInodeById(int inode_id) throws IOException {
		int base_length = gdt[0].inode_table * 1024;
		int block_index = inode_id / 11;
		int in_index = inode_id % 11;
		int length = base_length + block_index*1024 + in_index*93;
		byte[] buffer = new byte[88];
		ext.seek(length);
		ext.read(buffer);
		return byteToNode(buffer);
	}
	
	// �ڿ�idΪstart_index�ļ�ӿ���õ�count��block��
	public int[] getIndirectBlock(int start_index, int count) throws IOException {
		int[] result = new int[count];
		int base_length = (1+1+1+32+8+5957)*1024;
		base_length += start_index*1024;
		ext.seek(base_length);
		for(int i = 0; i < count; i++) {
			byte[] buffer = new byte[4];
			ext.read(buffer);
			result[i] = byteToInt(buffer, 0, 4);
		}
		return result;
	}
	
	// ����i�ڵ���ҿտ�
	public int[] getBlocksByInode(INode node) throws IOException {
		int blocks = node.blocks;
		int[] block = new int[blocks];
		for(int i = 0; i < 12 && i < blocks; i++) {
			block[i] = node.block[i];
		}
		if(blocks <= 12) {
			return block;
		}else if(blocks > 12 && blocks <= 12+256) {
			int first_indirect_block = block[12];	// ���ݿ� ���256�����
			int[] temp = getIndirectBlock(first_indirect_block, blocks-12);
			for(int i = 0; i < blocks-12; i++) {
				block[12+i] = temp[i];
			}
			return block;
		} else {
			System.out.println("ʹ�ö�����ӿ飬��ʱδд��");
			return null;
		}
	}
		
	// ��Ŀ¼��תΪbyte����
	public byte[] diritemToByte(DirItem diritem) {
		byte[] result = new byte[16];
		ArrayList<byte[]> temp = new ArrayList<byte[]>();
		temp.add(intToByte(diritem.inode, 2));
		int temp_value = diritem.name_len << 4 | diritem.file_type;
		temp.add(intToByte(temp_value, 1));
		for(int i = 0; i < 13; i++) {
			temp.add(intToByte(diritem.name[0], 1));
		}
		int k = 0;
		for(int i = 0; i < temp.size(); i++) {
			for(int j = 0; j < temp.get(i).length; j++) {
				result[k++] = temp.get(i)[j];
			}
		}
		return result;
	}
	
	// ji
	
	
	// ��i�ڵ��Ϊinode_index��Ŀ¼���Ŀ¼��item
	public boolean insertToDir(int inode_index, DirItem item) throws IOException {
		// ���Ҹ�i�ڵ���ռ�Ŀ��
		int[] block = getBlocksByInode(getInodeById(inode_index));
		int base_length = (1+1+1+32+8+5957)*1024;
		int free_dir_item = -1;
		int i;
		for(i = 0; i < block.length; i++) {
			int length = base_length + block[i]*1024;
			int item_index = 0;
			if(i == 0) {
				item_index = 2;
			}
			length += item_index*16;
			ext.seek(length);
			// Ѱ�ҵ�һ���յ�Ŀ¼��
			byte[] buffer = new byte[16];
			int flag = 0;
			while(flag != 0 && item_index < 64) {
				ext.read(buffer);
				int inode = byteToInt(buffer, 0, 2);
				if(inode == 0) {
					free_dir_item = item_index;
					flag = 1;
				}
				item_index++;
			}
			if(flag == 1) {
				break;
			}
		}
		if(free_dir_item == -1) {
			return false;
		} else {
			// �����
			int length = base_length + block[i]*1024 + free_dir_item*16;
			
		}
		return false;
	}
	
	// ������ʼ���Ϊdir_index��Ŀ¼����Ϊname���ļ���Ŀ¼����ʼ���ݿ��
	public DirItem getDirItemByName(int dir_index, String name) {
		return new DirItem();
	}
	
	// ������ʼ���Ϊdir_index��Ŀ¼�ĵ�item_index��Ŀ¼��
	public DirItem getDirItemByIndex(int dir_index, int item_index) {
		return new DirItem();
	}
	
	// ������ʼ���Ϊdir_index��Ŀ¼����Ϊname���ļ���Ŀ¼����i�ڵ��id
	public int getINodeIDByName(int dir_index, String name) {
		return 0;
	}
	
	// ������ʼ���Ϊdir_index��Ŀ¼�ĵ�item_index�ļ���i�ڵ�id 
	public int getINodeIDByIndex(int dir_index, int item_index) {
		return 0;
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
	
	// ����1������i�ڵ��
	public int getFreeInode() {
		return 0;
	}
}
