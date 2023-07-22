import React, { useEffect, useState } from 'react';
import CopyToClipboard from 'react-copy-to-clipboard';
import Button from '../atom/Button';
import { ReactComponent as KakaoIcon } from '@/assets/icons/kakaoauth.svg';
import { ReactComponent as ShareIcon } from '@/assets/icons/share-link.svg';
import useToast from '@/hooks/useToast';
import instance from '@/queries/axiosinstance';
import { UserGetRequest } from '@/types/api/users-types';
import RoundButton from './RoundButton';
import { BASE_URL } from '@/queries';

const SharesButtons = ({ scheduleId }: { scheduleId: number }) => {
  const [shareLink, setShareLink] = useState('');
  const [userInfo, setUserInfo] = useState<UserGetRequest>();
  const toast = useToast();

  useEffect(() => {
    const getUserInfo = async () => {
      await instance.get('/api/users').then((res) => {
        const { memberId, email } = res.data.data;

        setShareLink(
          `${BASE_URL}plan/detail/${scheduleId}/share?id=${memberId}&email=${email}`
          // `http://localhost:5173/plan/detail/${scheduleId}/share?id=${memberId}&email=${email}`
        );
      });
    };

    getUserInfo();

    // 이후 배포주소로 변경 필요
  }, []);

  return (
    <>
      <CopyToClipboard
        text={shareLink}
        onCopy={() => toast({ content: '링크 복사 완료!', type: 'success' })}
      >
        <RoundButton>
          <ShareIcon fill="#4568DC" />
        </RoundButton>
      </CopyToClipboard>

      <RoundButton>
        <KakaoIcon />
      </RoundButton>
    </>
  );
};

export default SharesButtons;
