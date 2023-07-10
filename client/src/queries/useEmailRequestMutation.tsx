import { useMutation } from '@tanstack/react-query';
import BASE_URL from './BASE_URL';

const postEmailRequest = async (email: string) => {
  const response = await fetch(`${BASE_URL}/api/email/signup`, {
    method: 'POST',
    body: JSON.stringify(email),
  });
  const result = await response.json();
  const ok = response.ok;
  return {
    result,
    ok,
    response,
  };
};

const useEmailRequestMutation = () => {
  const emailRequest = useMutation({
    mutationFn: (email: string) => postEmailRequest(email),
  });

  return emailRequest;
};

export default useEmailRequestMutation;
