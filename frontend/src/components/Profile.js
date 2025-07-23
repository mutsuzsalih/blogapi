import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { postsAPI } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import { 
  User, 
  Mail, 
  Calendar, 
  BookOpen, 
  Tag,
  Edit,
  Trash2
} from 'lucide-react';
import { toast } from 'react-toastify';

const Profile = () => {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [userPosts, setUserPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [deleteLoading, setDeleteLoading] = useState(null);
  const [showDeleteModal, setShowDeleteModal] = useState(null);

  useEffect(() => {
    if (user) {
      fetchUserPosts();
    }
  }, [user]); // eslint-disable-line react-hooks/exhaustive-deps

  const fetchUserPosts = async () => {
    try {
      setLoading(true);
      const response = await postsAPI.getPostsByUser(user.id);
      setUserPosts(response.data || []);
    } catch (error) {
      if (error.response) {
        toast.error(t('messages.error.userPostsLoadError'));
      } else if (error.request) {
        console.warn(t('forms.serverOfflineShort'));
      } else {
        console.error('Error fetching user posts:', error);
      }
      setUserPosts([]);
    } finally {
      setLoading(false);
    }
  };

  const handleDeletePost = async (postId) => {
    try {
      setDeleteLoading(postId);
      await postsAPI.deletePost(postId);
      toast.success(t('messages.success.postDeleted'));
      setUserPosts(prev => prev.filter(post => post.id !== postId));
    } catch (error) {
      console.error('Error deleting post:', error);
      toast.error(t('messages.error.postDeleteError'));
    } finally {
      setDeleteLoading(null);
      setShowDeleteModal(null);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('tr-TR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const truncateContent = (content, maxLength = 150) => {
    if (!content) return '';
    
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = content;
    const textContent = tempDiv.textContent || tempDiv.innerText || '';
    
    return textContent.length > maxLength 
      ? textContent.substring(0, maxLength) + '...'
      : textContent;
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 bg-gray-50 dark:bg-gray-900 min-h-screen">
      <div className="bg-white dark:bg-gray-800 shadow rounded-lg mb-8 border border-gray-200 dark:border-gray-700">
        <div className="px-6 py-8">
          <div className="flex items-center space-x-6">
            <div className="flex-shrink-0">
              <div className="h-20 w-20 bg-primary-100 dark:bg-primary-900/20 rounded-full flex items-center justify-center">
                <User className="h-10 w-10 text-primary-600 dark:text-primary-400" />
              </div>
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                {user?.username}
              </h1>
              <div className="flex items-center space-x-4 mt-2">
                <div className="flex items-center space-x-2">
                  <Mail className="h-4 w-4 text-gray-400 dark:text-gray-500" />
                  <span className="text-sm text-gray-600 dark:text-gray-300">{user?.email}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="bg-white dark:bg-gray-800 shadow rounded-lg border border-gray-200 dark:border-gray-700">
        <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h2 className="text-lg font-medium text-gray-900 dark:text-gray-100">
            {t('forms.myPosts')}
          </h2>
        </div>
        
        <div className="p-6">
          {userPosts.length === 0 ? (
            <div className="text-center py-12">
              <BookOpen className="h-16 w-16 text-gray-400 dark:text-gray-500 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">
                {t('forms.noPosts')}
              </h3>
              <p className="text-gray-600 dark:text-gray-300 mb-6">
                {t('forms.writeFirst')}
              </p>
              <Link
                to="/create-post"
                className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
              >
                <BookOpen className="h-5 w-5 mr-2" />
                {t('forms.writeFirstPost')}
              </Link>
            </div>
          ) : (
            <div className="space-y-6">
              {userPosts.map((post) => (
                <div key={post.id} className="border border-gray-200 dark:border-gray-700 rounded-lg p-6 bg-white dark:bg-gray-800">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center space-x-2">
                      <Calendar className="h-4 w-4 text-gray-400 dark:text-gray-500" />
                      <span className="text-sm text-gray-600 dark:text-gray-300">
                        {formatDate(post.createdAt)}
                      </span>
                    </div>
                    
                    {/* Edit/Delete Buttons */}
                    <div className="flex items-center space-x-2">
                      <Link
                        to={`/edit-post/${post.id}`}
                        className="inline-flex items-center px-3 py-1.5 text-sm font-medium text-primary-600 dark:text-primary-400 bg-primary-50 dark:bg-primary-900/20 border border-primary-200 dark:border-primary-700 rounded-md hover:bg-primary-100 dark:hover:bg-primary-900/30"
                      >
                        <Edit className="h-3 w-3 mr-1" />
                        {t('forms.editPostButton')}
                      </Link>
                      <button
                        onClick={() => setShowDeleteModal(post.id)}
                        className="inline-flex items-center px-3 py-1.5 text-sm font-medium text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-700 rounded-md hover:bg-red-100 dark:hover:bg-red-900/30"
                      >
                        <Trash2 className="h-3 w-3 mr-1" />
                        {t('ui.delete')}
                      </button>
                    </div>
                  </div>

                  <h3 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-2">
                    <Link 
                      to={`/post/${post.id}`}
                      className="hover:text-primary-600 dark:hover:text-primary-400 transition-colors"
                    >
                      {post.title}
                    </Link>
                  </h3>

                  {post.content && (
                    <p className="text-gray-600 dark:text-gray-300 mb-4">
                      {truncateContent(post.content)}
                    </p>
                  )}

                  {post.tags && post.tags.length > 0 && (
                    <div className="flex flex-wrap gap-2">
                      {post.tags.map((tag, index) => (
                        <span
                          key={index}
                          className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200"
                        >
                          <Tag className="h-3 w-3 mr-1" />
                          {tag.name}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div className="fixed inset-0 bg-gray-600 dark:bg-gray-900 bg-opacity-50 dark:bg-opacity-80 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white dark:bg-gray-800 border-gray-200 dark:border-gray-700">
            <div className="mt-3 text-center">
              <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 dark:bg-red-900/20">
                <Trash2 className="h-6 w-6 text-red-600 dark:text-red-400" />
              </div>
              <h3 className="text-lg leading-6 font-medium text-gray-900 dark:text-gray-100 mt-4">
                {t('forms.deletePostConfirm')}
              </h3>
              <div className="mt-2 px-7 py-3">
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  {t('forms.deletePostConfirmText')}
                </p>
              </div>
              <div className="items-center px-4 py-3">
                <div className="flex space-x-3">
                  <button
                    onClick={() => setShowDeleteModal(null)}
                    className="px-4 py-2 bg-gray-300 dark:bg-gray-600 text-gray-700 dark:text-gray-300 text-sm font-medium rounded-md hover:bg-gray-400 dark:hover:bg-gray-500 w-full"
                  >
                    {t('ui.cancel')}
                  </button>
                  <button
                    onClick={() => handleDeletePost(showDeleteModal)}
                    disabled={deleteLoading === showDeleteModal}
                    className="px-4 py-2 bg-red-600 text-white text-sm font-medium rounded-md hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed w-full"
                  >
                    {deleteLoading === showDeleteModal ? t('ui.deleting') : t('ui.delete')}
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

export default Profile; 