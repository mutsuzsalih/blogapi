import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { postsAPI, tagsAPI } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import { toast } from 'react-toastify';
import { Save, X, Tag } from 'lucide-react';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';

const EditPost = () => {
  const { t } = useTranslation();
  const { id } = useParams();
  const [formData, setFormData] = useState({
    title: '',
    content: '',
    tagIds: []
  });
  const [availableTags, setAvailableTags] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pageLoading, setPageLoading] = useState(true);
  const [errors, setErrors] = useState({});

  const { isAdmin } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    fetchTags();
    fetchPost();
  }, [id]); // eslint-disable-line react-hooks/exhaustive-deps

  const fetchPost = async () => {
    try {
      const response = await postsAPI.getPostById(id);
      const post = response.data;
      setFormData({
        title: post.title,
        content: post.content,
        tagIds: post.tags ? post.tags.map(tag => tag.id) : []
      });
    } catch (error) {
      console.error('Error fetching post:', error);
      toast.error(t('messages.error.postsLoadError'));
      navigate('/');
    } finally {
      setPageLoading(false);
    }
  };

  const fetchTags = async () => {
    try {
      const response = await tagsAPI.getAllTags();
      setAvailableTags(response.data || []);
    } catch (error) {
      console.error('Error fetching tags:', error);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const handleContentChange = (content) => {
    setFormData(prev => ({
      ...prev,
      content: content
    }));
    
    if (errors.content) {
      setErrors(prev => ({
        ...prev,
        content: ''
      }));
    }
  };

  const handleTagToggle = (tagId) => {
    setFormData(prev => ({
      ...prev,
      tagIds: prev.tagIds.includes(tagId)
        ? prev.tagIds.filter(id => id !== tagId)
        : [...prev.tagIds, tagId]
    }));
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.title.trim()) {
      newErrors.title = t('validation.title');
    } else if (formData.title.length < 5) {
      newErrors.title = t('validation.titleMin');
    }

    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = formData.content;
    const textContent = tempDiv.textContent || tempDiv.innerText || '';

    if (!textContent.trim()) {
      newErrors.content = t('validation.content');
    } else if (textContent.length < 50) {
      newErrors.content = t('validation.contentMin');
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    try {
      setLoading(true);
      await postsAPI.updatePost(id, formData);
      toast.success(t('messages.success.postUpdated'));
      navigate(`/post/${id}`);
    } catch (error) {
      console.error('Error updating post:', error);
      
      let errorMessage = t('messages.error.postUpdateError');
      
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.response?.data?.validationErrors) {
        const validationErrors = error.response.data.validationErrors;
        const firstError = Object.values(validationErrors)[0];
        errorMessage = firstError || errorMessage;
      } else if (error.request) {
        errorMessage = t('messages.error.serverConnection');
      } else {
        errorMessage = t('messages.error.unexpected');
      }
      
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const selectedTags = availableTags.filter(tag => formData.tagIds.includes(tag.id));

  if (pageLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 bg-gray-50 dark:bg-gray-900 min-h-screen">
      <div className="bg-white dark:bg-gray-800 shadow rounded-lg border border-gray-200 dark:border-gray-700">
        <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">{t('forms.editPostTitle')}</h1>
          <p className="mt-1 text-sm text-gray-600 dark:text-gray-300">
            {t('forms.newPostSubtitle')}
          </p>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          {/* Title */}
          <div>
            <label htmlFor="title" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              {t('posts.title')} *
            </label>
            <input
              type="text"
              id="title"
              name="title"
              value={formData.title}
              onChange={handleChange}
              className={`w-full px-3 py-2 border rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-primary-500 ${
                errors.title ? 'border-red-300 dark:border-red-500' : 'border-gray-300 dark:border-gray-600'
              }`}
              placeholder={t('forms.titlePlaceholder')}
            />
            {errors.title && (
              <p className="mt-1 text-sm text-red-600 dark:text-red-400">{errors.title}</p>
            )}
          </div>

          {/* Content */}
          <div>
            <label htmlFor="content-editor" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              {t('posts.content')} *
            </label>
            <div className={`border rounded-md ${errors.content ? 'border-red-300 dark:border-red-500' : 'border-gray-300 dark:border-gray-600'}`}>
              <ReactQuill
                id="content-editor"
                value={formData.content}
                onChange={handleContentChange}
                placeholder={t('forms.contentPlaceholder')}
                modules={{
                  toolbar: [
                    [{ 'header': [1, 2, 3, false] }],
                    ['bold', 'italic', 'underline', 'strike'],
                    [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                    ['blockquote', 'code-block'],
                    ['link'],
                    ['clean']
                  ]
                }}
                formats={[
                  'header', 'bold', 'italic', 'underline', 'strike',
                  'list', 'bullet', 'blockquote', 'code-block', 'link'
                ]}
                style={{ 
                  minHeight: '200px',
                  backgroundColor: 'white'
                }}
                className="dark:bg-gray-700 [&_.ql-editor]:dark:bg-gray-700 [&_.ql-editor]:dark:text-gray-100 [&_.ql-editor]:dark:placeholder-gray-400 [&_.ql-toolbar]:dark:bg-gray-600 [&_.ql-toolbar]:dark:border-gray-500 [&_.ql-stroke]:dark:stroke-gray-300 [&_.ql-fill]:dark:fill-gray-300 [&_.ql-even]:dark:fill-gray-300 [&_.ql-picker-label]:dark:color-gray-300 [&_.ql-picker-options]:dark:bg-gray-600 [&_.ql-picker-item]:dark:color-gray-300"
              />
            </div>
            {errors.content && (
              <p className="mt-1 text-sm text-red-600 dark:text-red-400">{errors.content}</p>
            )}
            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              {(() => {
                const tempDiv = document.createElement('div');
                tempDiv.innerHTML = formData.content;
                const textContent = tempDiv.textContent || tempDiv.innerText || '';
                return textContent.length;
              })()} {t('forms.characterCount', { current: '', min: 50 }).replace('{current} ', '')}
            </p>
          </div>

          {/* Tags */}
          <div>
            <div className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              {t('posts.tags')}
            </div>
            
            {/* Selected Tags */}
            {selectedTags.length > 0 && (
              <div className="mb-3">
                <p className="text-sm text-gray-600 dark:text-gray-300 mb-2">{t('forms.selectedTags')}</p>
                <div className="flex flex-wrap gap-2">
                  {selectedTags.map(tag => (
                    <span
                      key={tag.id}
                      className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-primary-100 dark:bg-primary-900 text-primary-800 dark:text-primary-200"
                    >
                      <Tag className="h-3 w-3 mr-1" />
                      {tag.name}
                      <button
                        type="button"
                        onClick={() => handleTagToggle(tag.id)}
                        className="ml-2 text-primary-600 dark:text-primary-400 hover:text-primary-800 dark:hover:text-primary-200"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </span>
                  ))}
                </div>
              </div>
            )}

            {/* Available Tags */}
            <div className="space-y-3">
              <p className="text-sm text-gray-600 dark:text-gray-300">{t('forms.availableTags')}</p>
              {availableTags.length === 0 ? (
                <div className="text-center py-4">
                  <p className="text-sm text-gray-500 dark:text-gray-400 mb-2">
                    {t('forms.noTags')}
                  </p>
                  {isAdmin && (
                    <p className="text-xs text-gray-400 dark:text-gray-500">
                      Etiket oluşturmak için Admin Panel &gt; Etiket Yönetimi'ne gidin.
                    </p>
                  )}
                </div>
              ) : (
                <div className="flex flex-wrap gap-2">
                  {availableTags
                    .filter(tag => !formData.tagIds.includes(tag.id))
                    .map(tag => (
                      <button
                        key={tag.id}
                        type="button"
                        onClick={() => handleTagToggle(tag.id)}
                        className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200 hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"
                      >
                        <Tag className="h-3 w-3 mr-1" />
                        {tag.name}
                      </button>
                    ))}
                </div>
              )}
            </div>
          </div>

          {/* Submit Buttons */}
          <div className="flex space-x-3 pt-4">
            <button
              type="button"
              onClick={() => navigate(-1)}
              className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-md hover:bg-gray-50 dark:hover:bg-gray-600"
            >
              {t('ui.cancel')}
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex items-center px-6 py-2 text-sm font-medium text-white bg-primary-600 border border-transparent rounded-md hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                <div className="flex items-center">
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  {t('forms.updating')}
                </div>
              ) : (
                <>
                  <Save className="h-4 w-4 mr-2" />
                  {t('forms.updatePost')}
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditPost; 