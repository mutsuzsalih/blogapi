import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { postsAPI } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import { 
  Calendar, 
  User, 
  Tag, 
  ArrowLeft,
  Edit,
  Trash2
} from 'lucide-react';
import { toast } from 'react-toastify';

const PostDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const { user, isAuthenticated } = useAuth();

  useEffect(() => {
    fetchPost();
  }, [id]); // eslint-disable-line react-hooks/exhaustive-deps

  const fetchPost = async () => {
    try {
      setLoading(true);
      const response = await postsAPI.getPostById(id);
      setPost(response.data);
    } catch (error) {
      console.error('Error fetching post:', error);
      
      let errorMessage;
      
      if (error.response?.status === 404) {
        errorMessage = 'Blog yazısı bulunamadı';
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.request) {
        errorMessage = 'Sunucuya ulaşılamıyor';
      } else {
        errorMessage = 'Beklenmeyen bir hata oluştu';
      }
      
      toast.error(errorMessage);
      navigate('/');
    } finally {
      setLoading(false);
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

  const handleDeletePost = async () => {
    try {
      setDeleteLoading(true);
      await postsAPI.deletePost(id);
      toast.success('Blog yazısı başarıyla silindi!');
      navigate('/');
    } catch (error) {
      console.error('Error deleting post:', error);
      toast.error('Blog yazısı silinirken hata oluştu');
    } finally {
      setDeleteLoading(false);
      setShowDeleteModal(false);
    }
  };

  const canEditPost = isAuthenticated && user && post && (user.username === post.authorUsername || user.role === 'ADMIN');

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (!post) {
    return (
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">Blog yazısı bulunamadı</h1>
          <Link to="/" className="text-primary-600 hover:text-primary-700 font-medium">
            Ana sayfaya dön
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-6">
        <button
          onClick={() => navigate('/')}
          className="inline-flex items-center text-sm font-medium text-gray-600 hover:text-gray-900"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Ana sayfaya dön
        </button>
      </div>

      <article className="bg-white dark:bg-gray-800 shadow rounded-lg overflow-hidden border border-gray-200 dark:border-gray-700">
        <div className="px-6 py-8 border-b border-gray-200 dark:border-gray-700">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-2">
                <User className="h-5 w-5 text-gray-400 dark:text-gray-500" />
                <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
                  {post.authorUsername}
                </span>
              </div>
              <div className="flex items-center space-x-2">
                <Calendar className="h-5 w-5 text-gray-400 dark:text-gray-500" />
                <time className="text-sm text-gray-600 dark:text-gray-400">
                  {formatDate(post.createdAt)}
                </time>
              </div>
            </div>
            
            {/* Edit/Delete Buttons */}
            {canEditPost && (
              <div className="flex items-center space-x-2">
                <Link
                  to={`/edit-post/${post.id}`}
                  className="inline-flex items-center px-3 py-2 text-sm font-medium text-primary-600 dark:text-primary-400 bg-primary-50 dark:bg-primary-900/20 border border-primary-200 dark:border-primary-700 rounded-md hover:bg-primary-100 dark:hover:bg-primary-900/30"
                >
                  <Edit className="h-4 w-4 mr-1" />
                  Düzenle
                </Link>
                <button
                  onClick={() => setShowDeleteModal(true)}
                  className="inline-flex items-center px-3 py-2 text-sm font-medium text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-700 rounded-md hover:bg-red-100 dark:hover:bg-red-900/30"
                >
                  <Trash2 className="h-4 w-4 mr-1" />
                  Sil
                </button>
              </div>
            )}
          </div>

          <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-6">
            {post.title}
          </h1>

          {post.tags && post.tags.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {post.tags.map((tag) => (
                <span
                  key={tag.id}
                  className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-primary-100 dark:bg-primary-900 text-primary-800 dark:text-primary-200"
                >
                  <Tag className="h-3 w-3 mr-1" />
                  {tag.name}
                </span>
              ))}
            </div>
          )}
        </div>

        <div className="px-6 py-8">
          <div className="prose prose-lg max-w-none dark:prose-invert">
            <div 
              className="text-gray-800 dark:text-gray-200 leading-relaxed [&_h1]:text-gray-900 [&_h1]:dark:text-gray-100 [&_h2]:text-gray-900 [&_h2]:dark:text-gray-100 [&_h3]:text-gray-900 [&_h3]:dark:text-gray-100 [&_p]:text-gray-800 [&_p]:dark:text-gray-200 [&_strong]:text-gray-900 [&_strong]:dark:text-gray-100 [&_blockquote]:text-gray-700 [&_blockquote]:dark:text-gray-300 [&_code]:text-gray-900 [&_code]:dark:text-gray-100 [&_code]:bg-gray-100 [&_code]:dark:bg-gray-700"
              dangerouslySetInnerHTML={{ __html: post.content }}
            />
          </div>
        </div>
      </article>

      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div className="fixed inset-0 bg-gray-600 dark:bg-gray-900 bg-opacity-50 dark:bg-opacity-80 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white dark:bg-gray-800 border-gray-200 dark:border-gray-700">
            <div className="mt-3 text-center">
              <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 dark:bg-red-900/20">
                <Trash2 className="h-6 w-6 text-red-600 dark:text-red-400" />
              </div>
              <h3 className="text-lg leading-6 font-medium text-gray-900 dark:text-gray-100 mt-4">
                Blog Yazısını Sil
              </h3>
              <div className="mt-2 px-7 py-3">
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  Bu blog yazısını silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.
                </p>
              </div>
              <div className="items-center px-4 py-3">
                <div className="flex space-x-3">
                  <button
                    onClick={() => setShowDeleteModal(false)}
                    className="px-4 py-2 bg-gray-300 dark:bg-gray-600 text-gray-700 dark:text-gray-300 text-sm font-medium rounded-md hover:bg-gray-400 dark:hover:bg-gray-500 w-full"
                  >
                    İptal
                  </button>
                  <button
                    onClick={handleDeletePost}
                    disabled={deleteLoading}
                    className="px-4 py-2 bg-red-600 text-white text-sm font-medium rounded-md hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed w-full"
                  >
                    {deleteLoading ? 'Siliniyor...' : 'Sil'}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PostDetail; 