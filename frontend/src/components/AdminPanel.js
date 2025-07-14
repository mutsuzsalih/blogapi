import React, { useState, useEffect, useCallback } from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { postsAPI, tagsAPI, usersAPI } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import { 
  Calendar, 
  User, 
  Edit, 
  Trash2, 
  ChevronLeft, 
  ChevronRight, 
  Shield,
  BookOpen,
  AlertTriangle,
  Tag,
  Plus,
  Save,
  X,
  Users,
  Mail,
  Crown
} from 'lucide-react';
import { toast } from 'react-toastify';

const AdminPanel = () => {
  const { t } = useTranslation();
  const [activeTab, setActiveTab] = useState('posts');
  const [posts, setPosts] = useState([]);
  const [tags, setTags] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [deleteConfirm, setDeleteConfirm] = useState(null);
  const [editingTag, setEditingTag] = useState(null);
  const [newTagName, setNewTagName] = useState('');
  const [showNewTagForm, setShowNewTagForm] = useState(false);
  const { isAuthenticated, isAdmin } = useAuth();

  const pageSize = 10;

  const fetchPosts = useCallback(async () => {
    try {
      setLoading(true);
      const response = await postsAPI.getAllPosts(currentPage, pageSize);
      setPosts(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
      setTotalElements(response.data.totalElements || 0);
    } catch (error) {
      console.error('Error fetching posts:', error);
      toast.error(t('messages.error.postsLoadError'));
      setPosts([]);
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize]);

  const fetchTags = useCallback(async () => {
    try {
      setLoading(true);
      const response = await tagsAPI.getAllTags();
      setTags(response.data || []);
    } catch (error) {
      console.error('Error fetching tags:', error);
      toast.error(t('messages.error.tagsLoadError'));
      setTags([]);
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchUsers = useCallback(async () => {
    try {
      setLoading(true);
      const response = await usersAPI.getAllUsers();
      setUsers(response.data || []);
    } catch (error) {
      console.error('Error fetching users:', error);
      toast.error(t('messages.error.usersLoadError'));
      setUsers([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!isAuthenticated || !isAdmin) {
      return;
    }
    if (activeTab === 'posts') {
      fetchPosts();
    } else if (activeTab === 'tags') {
      fetchTags();
    } else if (activeTab === 'users') {
      fetchUsers();
    }
  }, [fetchPosts, fetchTags, fetchUsers, isAuthenticated, isAdmin, activeTab]);

  const handleDeletePost = async (postId) => {
    try {
      await postsAPI.deletePost(postId);
      toast.success(t('messages.success.postDeleted'));
      setPosts(posts.filter(post => post.id !== postId));
      setTotalElements(prev => prev - 1);
      setDeleteConfirm(null);
    } catch (error) {
      console.error('Error deleting post:', error);
      toast.error(t('messages.error.postDeleteError'));
    }
  };

  const handleCreateTag = async () => {
    if (!newTagName.trim()) {
      toast.error(t('messages.error.tagNameEmpty'));
      return;
    }

    try {
      const response = await tagsAPI.createTag({ name: newTagName.trim() });
      setTags([...tags, response.data]);
      setNewTagName('');
      setShowNewTagForm(false);
      toast.success(t('messages.success.tagCreated'));
    } catch (error) {
      console.error('Error creating tag:', error);
      toast.error(t('messages.error.tagCreateError'));
    }
  };

  const handleUpdateTag = async (tagId, newName) => {
    if (!newName.trim()) {
      toast.error(t('messages.error.tagNameEmpty'));
      return;
    }

    try {
      const response = await tagsAPI.updateTag(tagId, { name: newName.trim() });
      setTags(tags.map(tag => tag.id === tagId ? response.data : tag));
      setEditingTag(null);
      toast.success(t('messages.success.tagUpdated'));
    } catch (error) {
      console.error('Error updating tag:', error);
      toast.error(t('messages.error.tagUpdateError'));
    }
  };

  const handleDeleteTag = async (tagId) => {
    try {
      await tagsAPI.deleteTag(tagId);
      setTags(tags.filter(tag => tag.id !== tagId));
      setDeleteConfirm(null);
      toast.success(t('messages.success.tagDeleted'));
    } catch (error) {
      console.error('Error deleting tag:', error);
      toast.error(t('messages.error.tagDeleteError'));
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('tr-TR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const truncateContent = (content, maxLength = 100) => {
    return content && content.length > maxLength 
      ? content.substring(0, maxLength) + '...'
      : content;
  };

  const renderTabContent = () => {
    if (activeTab === 'posts') {
      return (
        <PostsManagement 
          posts={posts}
          totalPages={totalPages}
          currentPage={currentPage}
          setCurrentPage={setCurrentPage}
          deleteConfirm={deleteConfirm}
          setDeleteConfirm={setDeleteConfirm}
          handleDeletePost={handleDeletePost}
          formatDate={formatDate}
          truncateContent={truncateContent}
          pageSize={pageSize}
          totalElements={totalElements}
        />
      );
    }
    if (activeTab === 'tags') {
      return (
        <TagsManagement 
          tags={tags}
          editingTag={editingTag}
          setEditingTag={setEditingTag}
          newTagName={newTagName}
          setNewTagName={setNewTagName}
          showNewTagForm={showNewTagForm}
          setShowNewTagForm={setShowNewTagForm}
          handleCreateTag={handleCreateTag}
          handleUpdateTag={handleUpdateTag}
          deleteConfirm={deleteConfirm}
          setDeleteConfirm={setDeleteConfirm}
          handleDeleteTag={handleDeleteTag}
        />
      );
    }
    return (
      <UsersManagement 
        users={users}
        formatDate={formatDate}
      />
    );
  };

  if (!isAuthenticated || !isAdmin) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="text-center">
          <AlertTriangle className="h-16 w-16 text-red-500 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-gray-900 dark:text-gray-100 mb-2">
            {t('admin.accessDenied')}
          </h2>
          <p className="text-gray-600 dark:text-gray-300 mb-6">
            {t('admin.adminRequired')}
          </p>
          <Link
            to="/"
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
          >
            {t('admin.backToHome')}
          </Link>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 bg-gray-50 dark:bg-gray-900 min-h-screen">
      {/* Header */}
      <div className="mb-8">
        <div className="px-6 py-8 border-b border-gray-200 dark:border-gray-700">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-2">
            {t('admin.title')}
          </h1>
          <p className="text-gray-600 dark:text-gray-300">
            {t('admin.subtitle')}
          </p>
        </div>
        <div className="px-6 py-4 flex justify-end">
          <Link
            to="/create-post"
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
          >
            <BookOpen className="h-4 w-4 mr-2" />
            {t('admin.newPost')}
          </Link>
        </div>
      </div>

      {/* Tabs */}
      <div className="mb-8">
        <div className="border-b border-gray-200 dark:border-gray-700">
          <nav className="-mb-px flex space-x-8">
            <button
              onClick={() => setActiveTab('posts')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'posts'
                  ? 'border-primary-500 text-primary-600 dark:text-primary-400'
                  : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:border-gray-300 dark:hover:border-gray-600'
              }`}
            >
              <BookOpen className="h-4 w-4 inline mr-2" />
              {t('admin.postsTab')} ({totalElements})
            </button>
            <button
              onClick={() => setActiveTab('tags')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'tags'
                  ? 'border-primary-500 text-primary-600 dark:text-primary-400'
                  : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:border-gray-300 dark:hover:border-gray-600'
              }`}
            >
              <Tag className="h-4 w-4 inline mr-2" />
              {t('admin.tagsTab')} ({tags.length})
            </button>
            <button
              onClick={() => setActiveTab('users')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'users'
                  ? 'border-primary-500 text-primary-600 dark:text-primary-400'
                  : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:border-gray-300 dark:hover:border-gray-600'
              }`}
            >
              <Users className="h-4 w-4 inline mr-2" />
              {t('admin.usersTab')} ({users.length})
            </button>
          </nav>
        </div>
      </div>

      {/* Tab Content */}
      {renderTabContent()}
    </div>
  );
};

const PostsManagement = ({ 
  posts, totalPages, currentPage, setCurrentPage, deleteConfirm, setDeleteConfirm,
  handleDeletePost, formatDate, truncateContent, pageSize, totalElements 
}) => {
  const { t } = useTranslation();
  
  return (
  <div className="bg-white dark:bg-gray-800 rounded-lg shadow border border-gray-200 dark:border-gray-700">
    <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
      <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">
        {t('admin.postsManagement')}
      </h3>
    </div>
    
    {posts.length === 0 ? (
      <div className="text-center py-12">
        <BookOpen className="h-16 w-16 text-gray-400 dark:text-gray-500 mx-auto mb-4" />
        <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">
          {t('admin.postsEmpty')}
        </h3>
        <p className="text-gray-600 dark:text-gray-300 mb-6">
          {t('admin.postsEmptySubtitle')}
        </p>
        <Link
          to="/create-post"
          className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
        >
          <BookOpen className="h-5 w-5 mr-2" />
          {t('admin.writeFirstPost')}
        </Link>
      </div>
    ) : (
      <>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
            <thead className="bg-gray-50 dark:bg-gray-700">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  {t('admin.post')}
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  {t('admin.author')}
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  {t('admin.date')}
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  {t('admin.actions')}
                </th>
              </tr>
            </thead>
            <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
              {posts.map((post) => (
                <tr key={post.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                  <td className="px-6 py-4">
                    <div>
                      <div className="text-sm font-medium text-gray-900 dark:text-gray-100">
                        <Link 
                          to={`/post/${post.id}`}
                          className="hover:text-primary-600 dark:hover:text-primary-400"
                        >
                          {post.title}
                        </Link>
                      </div>
                      <div className="text-sm text-gray-500 dark:text-gray-400">
                        {truncateContent(post.content)}
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <User className="h-4 w-4 text-gray-400 dark:text-gray-500 mr-2" />
                      <span className="text-sm text-gray-900 dark:text-gray-100">{post.authorUsername}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <Calendar className="h-4 w-4 text-gray-400 dark:text-gray-500 mr-2" />
                      <span className="text-sm text-gray-900 dark:text-gray-100">{formatDate(post.createdAt)}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <div className="flex space-x-2">
                      <Link
                        to={`/edit-post/${post.id}`}
                        className="text-indigo-600 hover:text-indigo-900 dark:text-indigo-400 dark:hover:text-indigo-300 flex items-center"
                      >
                        <Edit className="h-4 w-4 mr-1" />
                        Düzenle
                      </Link>
                      <button
                        onClick={() => setDeleteConfirm({ type: 'post', id: post.id })}
                        className="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300 flex items-center"
                      >
                        <Trash2 className="h-4 w-4 mr-1" />
                        Sil
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="px-6 py-4 border-t border-gray-200 dark:border-gray-700">
            <div className="flex items-center justify-between">
              <div className="text-sm text-gray-700 dark:text-gray-300">
                Toplam <span className="font-medium">{totalElements}</span> yazıdan{' '}
                <span className="font-medium">{currentPage * pageSize + 1}</span> -{' '}
                <span className="font-medium">
                  {Math.min((currentPage + 1) * pageSize, totalElements)}
                </span> arası gösteriliyor
              </div>
              <div className="flex space-x-2">
                <button
                  onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                  disabled={currentPage === 0}
                  className="flex items-center px-3 py-2 text-sm font-medium text-gray-500 dark:text-gray-300 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-md hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <ChevronLeft className="h-4 w-4 mr-1" />
                  Önceki
                </button>
                <button
                  onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                  disabled={currentPage === totalPages - 1}
                  className="flex items-center px-3 py-2 text-sm font-medium text-gray-500 dark:text-gray-300 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-md hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Sonraki
                  <ChevronRight className="h-4 w-4 ml-1" />
                </button>
              </div>
            </div>
          </div>
        )}
      </>
    )}

    {/* Delete Confirmation Modal for Posts */}
    {deleteConfirm?.type === 'post' && (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md border border-gray-200 dark:border-gray-700">
          <div className="flex items-center mb-4">
            <AlertTriangle className="h-6 w-6 text-red-500 mr-3" />
            <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">
              Yazıyı Sil
            </h3>
          </div>
          <p className="text-gray-600 dark:text-gray-300 mb-6">
            Bu blog yazısını silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.
          </p>
          <div className="flex space-x-3">
            <button
              onClick={() => setDeleteConfirm(null)}
              className="flex-1 px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-md hover:bg-gray-200 dark:hover:bg-gray-600"
            >
              {t('common.cancel')}
            </button>
            <button
              onClick={() => handleDeletePost(deleteConfirm.id)}
              className="flex-1 px-4 py-2 text-sm font-medium text-white bg-red-600 border border-transparent rounded-md hover:bg-red-700"
            >
              {t('common.delete')}
            </button>
          </div>
        </div>
      </div>
    )}
  </div>
  )
};

const TagsManagement = ({ 
  tags, editingTag, setEditingTag, newTagName, setNewTagName, showNewTagForm, setShowNewTagForm,
  handleCreateTag, handleUpdateTag, deleteConfirm, setDeleteConfirm, handleDeleteTag 
}) => (
  <div className="bg-white dark:bg-gray-800 rounded-lg shadow border border-gray-200 dark:border-gray-700">
    <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">
          Etiket Yönetimi
        </h3>
        {!showNewTagForm && (
          <button
            onClick={() => setShowNewTagForm(true)}
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
          >
            <Plus className="h-4 w-4 mr-2" />
            Yeni Etiket
          </button>
        )}
      </div>
    </div>

    <div className="p-6">
      {/* New Tag Form */}
      {showNewTagForm && (
        <div className="mb-6 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg border border-gray-200 dark:border-gray-600">
          <h4 className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-3">Yeni Etiket Oluştur</h4>
          <div className="flex gap-3">
            <input
              type="text"
              value={newTagName}
              onChange={(e) => setNewTagName(e.target.value)}
              placeholder="Etiket adı..."
              className="flex-1 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-primary-500"
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault();
                  handleCreateTag();
                }
              }}
            />
            <button
              onClick={handleCreateTag}
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
            >
              <Save className="h-4 w-4 mr-1" />
              Kaydet
            </button>
            <button
              onClick={() => {
                setShowNewTagForm(false);
                setNewTagName('');
              }}
              className="inline-flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 text-sm font-medium rounded-md text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-700 hover:bg-gray-50 dark:hover:bg-gray-600"
            >
              <X className="h-4 w-4 mr-1" />
              İptal
            </button>
          </div>
        </div>
      )}

      {/* Tags List */}
      {tags.length === 0 ? (
        <div className="text-center py-12">
          <Tag className="h-16 w-16 text-gray-400 dark:text-gray-500 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">
            Henüz etiket yok
          </h3>
          <p className="text-gray-600 dark:text-gray-300 mb-6">
            İlk etiketi oluşturmaya ne dersin?
          </p>
          {!showNewTagForm && (
            <button
              onClick={() => setShowNewTagForm(true)}
              className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
            >
              <Plus className="h-5 w-5 mr-2" />
              İlk Etiketi Oluştur
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {tags.map((tag) => (
            <div key={tag.id} className="border border-gray-200 dark:border-gray-600 rounded-lg p-4 bg-gray-50 dark:bg-gray-700">
              {editingTag === tag.id ? (
                <EditTagForm 
                  tag={tag}
                  onSave={(newName) => handleUpdateTag(tag.id, newName)}
                  onCancel={() => setEditingTag(null)}
                />
              ) : (
                <div className="flex items-center justify-between">
                  <div className="flex items-center">
                    <Tag className="h-4 w-4 text-primary-600 dark:text-primary-400 mr-2" />
                    <span className="text-sm font-medium text-gray-900 dark:text-gray-100">{tag.name}</span>
                  </div>
                  <div className="flex space-x-2">
                    <button
                      onClick={() => setEditingTag(tag.id)}
                      className="text-indigo-600 hover:text-indigo-900 dark:text-indigo-400 dark:hover:text-indigo-300"
                    >
                      <Edit className="h-4 w-4" />
                    </button>
                    <button
                      onClick={() => setDeleteConfirm({ type: 'tag', id: tag.id })}
                      className="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>

    {/* Delete Confirmation Modal for Tags */}
    {deleteConfirm?.type === 'tag' && (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md border border-gray-200 dark:border-gray-700">
          <div className="flex items-center mb-4">
            <AlertTriangle className="h-6 w-6 text-red-500 mr-3" />
            <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">
              Etiketi Sil
            </h3>
          </div>
          <p className="text-gray-600 dark:text-gray-300 mb-6">
            Bu etiketi silmek istediğinizden emin misiniz? Bu işlem geri alınamaz ve etiketle ilişkili tüm bağlantılar kaldırılacak.
          </p>
          <div className="flex space-x-3">
            <button
              onClick={() => setDeleteConfirm(null)}
              className="flex-1 px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-md hover:bg-gray-200 dark:hover:bg-gray-600"
            >
              İptal
            </button>
            <button
              onClick={() => handleDeleteTag(deleteConfirm.id)}
              className="flex-1 px-4 py-2 text-sm font-medium text-white bg-red-600 border border-transparent rounded-md hover:bg-red-700"
            >
              Sil
            </button>
          </div>
        </div>
      </div>
    )}
  </div>
);

const EditTagForm = ({ tag, onSave, onCancel }) => {
  const [editName, setEditName] = useState(tag.name);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (editName.trim()) {
      onSave(editName.trim());
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex gap-2">
      <input
        type="text"
        value={editName}
        onChange={(e) => setEditName(e.target.value)}
        className="flex-1 px-2 py-1 border border-gray-300 dark:border-gray-600 rounded text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
        autoFocus
      />
      <button
        type="submit"
        className="text-green-600 hover:text-green-900 dark:text-green-400 dark:hover:text-green-300"
      >
        <Save className="h-4 w-4" />
      </button>
      <button
        type="button"
        onClick={onCancel}
        className="text-gray-600 hover:text-gray-900 dark:text-gray-400 dark:hover:text-gray-300"
      >
        <X className="h-4 w-4" />
      </button>
    </form>
  );
};

const UsersManagement = ({ users, formatDate }) => (
  <div className="bg-white dark:bg-gray-800 rounded-lg shadow border border-gray-200 dark:border-gray-700">
    <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
      <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">
        Kullanıcı Yönetimi
      </h3>
    </div>

    {users.length === 0 ? (
      <div className="text-center py-12">
        <Users className="h-16 w-16 text-gray-400 dark:text-gray-500 mx-auto mb-4" />
        <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">
          Henüz kullanıcı yok
        </h3>
        <p className="text-gray-600 dark:text-gray-300">
          İlk kullanıcı kayıt olduğunda burada görünecek.
        </p>
      </div>
    ) : (
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead className="bg-gray-50 dark:bg-gray-700">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                Kullanıcı
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                Email
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                Rol
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                ID
              </th>
            </tr>
          </thead>
          <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
            {users.map((userData) => (
              <tr key={userData.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 h-10 w-10">
                      <div className="h-10 w-10 rounded-full bg-primary-100 dark:bg-primary-900/20 flex items-center justify-center">
                        <User className="h-5 w-5 text-primary-600 dark:text-primary-400" />
                      </div>
                    </div>
                    <div className="ml-4">
                      <div className="text-sm font-medium text-gray-900 dark:text-gray-100">
                        {userData.username}
                      </div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <Mail className="h-4 w-4 text-gray-400 dark:text-gray-500 mr-2" />
                    <span className="text-sm text-gray-900 dark:text-gray-100">{userData.email}</span>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    {userData.role === 'ADMIN' ? (
                      <>
                        <Crown className="h-4 w-4 text-yellow-500 mr-2" />
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 dark:bg-yellow-900/20 text-yellow-800 dark:text-yellow-400">
                          Admin
                        </span>
                      </>
                    ) : (
                      <>
                        <User className="h-4 w-4 text-gray-400 dark:text-gray-500 mr-2" />
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200">
                          Kullanıcı
                        </span>
                      </>
                    )}
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className="text-sm text-gray-500 dark:text-gray-400">#{userData.id}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    )}
  </div>
);

PostsManagement.propTypes = {
  posts: PropTypes.array.isRequired,
  totalPages: PropTypes.number.isRequired,
  currentPage: PropTypes.number.isRequired,
  setCurrentPage: PropTypes.func.isRequired,
  deleteConfirm: PropTypes.object,
  setDeleteConfirm: PropTypes.func.isRequired,
  handleDeletePost: PropTypes.func.isRequired,
  formatDate: PropTypes.func.isRequired,
  truncateContent: PropTypes.func.isRequired,
  pageSize: PropTypes.number.isRequired,
  totalElements: PropTypes.number.isRequired
};

TagsManagement.propTypes = {
  tags: PropTypes.array.isRequired,
  editingTag: PropTypes.object,
  setEditingTag: PropTypes.func.isRequired,
  newTagName: PropTypes.string.isRequired,
  setNewTagName: PropTypes.func.isRequired,
  showNewTagForm: PropTypes.bool.isRequired,
  setShowNewTagForm: PropTypes.func.isRequired,
  handleCreateTag: PropTypes.func.isRequired,
  handleUpdateTag: PropTypes.func.isRequired,
  deleteConfirm: PropTypes.object,
  setDeleteConfirm: PropTypes.func.isRequired,
  handleDeleteTag: PropTypes.func.isRequired
};

EditTagForm.propTypes = {
  tag: PropTypes.object.isRequired,
  onSave: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired
};

UsersManagement.propTypes = {
  users: PropTypes.array.isRequired,
  formatDate: PropTypes.func.isRequired
};

export default AdminPanel; 