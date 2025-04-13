import React, { useState } from "react";
import {
  Button,
  Card,
  Input,
  List,
  Modal,
  Upload,
  message,
  Popconfirm,
  Empty,
} from "antd";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  UploadOutlined,
  FolderOpenOutlined,
} from "@ant-design/icons";
import { useStyles } from "../../style";
import { KnowledgeBase } from "../../types";
import { createKnowledgeBase, deleteKnowledgeBase } from "../../../../api/rag";

interface KnowledgeBaseListProps {
  knowledgeBases: KnowledgeBase[];
  activeKnowledgeBaseId: string | null;
  onSelect: (knowledgeBase: KnowledgeBase) => void;
  onUpdate: () => void;
}

const KnowledgeBaseList: React.FC<KnowledgeBaseListProps> = ({
  knowledgeBases,
  activeKnowledgeBaseId,
  onSelect,
  onUpdate,
}) => {
  const { styles } = useStyles();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [knowledgeBaseName, setKnowledgeBaseName] = useState("");
  const [fileList, setFileList] = useState<any[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const showModal = () => {
    setIsModalVisible(true);
    setKnowledgeBaseName("");
    setFileList([]);
  };

  const handleCancel = () => {
    setIsModalVisible(false);
  };

  const handleCreate = async () => {
    if (!knowledgeBaseName.trim()) {
      message.error("请输入知识库名称");
      return;
    }

    if (fileList.length === 0) {
      message.error("请至少上传一个文件");
      return;
    }

    setIsSubmitting(true);
    try {
      const files = fileList.map((file) => file.originFileObj);
      await createKnowledgeBase(knowledgeBaseName, files);
      message.success("知识库创建成功");
      setIsModalVisible(false);
      onUpdate();
    } catch (error) {
      console.error("创建知识库失败:", error);
      message.error("创建知识库失败");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteKnowledgeBase(id);
      message.success("知识库删除成功");
      onUpdate();
    } catch (error) {
      console.error("删除知识库失败:", error);
      message.error("删除知识库失败");
    }
  };

  return (
    <>
      <div className={styles.knowledgeBaseHeader}>
        <h3>知识库列表</h3>
        <Button type="primary" icon={<PlusOutlined />} onClick={showModal}>
          新建知识库
        </Button>
      </div>
      <div className={styles.knowledgeBaseList}>
        {knowledgeBases.length === 0 ? (
          <div className={styles.emptyContainer}>
            {/* <FolderOpenOutlined
              style={{ fontSize: 64, opacity: 0.6 }}
              className={styles.placeholderImage}
            /> */}
            <Empty
              description="暂无知识库"
              image={Empty.PRESENTED_IMAGE_SIMPLE}
            >
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={showModal}
              >
                新建知识库
              </Button>
            </Empty>
          </div>
        ) : (
          <List
            dataSource={knowledgeBases}
            renderItem={(item) => (
              <Card
                key={item.id}
                className={`${styles.knowledgeBaseCard} ${
                  activeKnowledgeBaseId === item.id ? styles.activeCard : ""
                }`}
                title={item.name}
                extra={
                  <Popconfirm
                    title="确定要删除这个知识库吗?"
                    onConfirm={() => handleDelete(item.id)}
                    okText="是"
                    cancelText="否"
                  >
                    <Button
                      type="text"
                      danger
                      icon={<DeleteOutlined />}
                      onClick={(e) => e.stopPropagation()}
                    />
                  </Popconfirm>
                }
                onClick={() => onSelect(item)}
              >
                <p>点击选择此知识库进行RAG查询</p>
              </Card>
            )}
          />
        )}
      </div>

      <Modal
        title="新建知识库"
        open={isModalVisible}
        onOk={handleCreate}
        onCancel={handleCancel}
        confirmLoading={isSubmitting}
        className={styles.uploadModal}
      >
        <Input
          placeholder="请输入知识库名称"
          value={knowledgeBaseName}
          onChange={(e) => setKnowledgeBaseName(e.target.value)}
          style={{ marginBottom: 16 }}
        />
        <Upload
          multiple
          beforeUpload={() => false}
          onChange={(info) => setFileList(info.fileList)}
          fileList={fileList}
        >
          <Button icon={<UploadOutlined />}>
            上传文件(支持PDF、DOCX、TXT)
          </Button>
        </Upload>
      </Modal>
    </>
  );
};

export default KnowledgeBaseList;
